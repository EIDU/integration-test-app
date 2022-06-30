package com.eidu.integration.test.app.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.LinkedList

/**
 * An asynchronous LRU cache.
 * @param maxItems the maximum number of items to store before evicting the least recently used one
 * @param generate the function that generates the value for a given key
 */
class AsyncCache<Key, Value>(
    private val maxItems: Int,
    private val generate: suspend (Key) -> Value,
    private val onEvict: suspend (Value) -> Unit = {}
) {
    /**
     * Used to box potentially nullable values because Map assigns special meaning to null.
     */
    private class Item<Value>(val value: Value)

    private val cachedItems = mutableMapOf<Key, Item<Value>>()
    private val cachedKeys = LinkedList<Key>()
    private val itemsInGeneration = mutableMapOf<Key, Deferred<Value>>()

    private val mutex = Mutex()

    /**
     * Gets the item with the given key, generating it by calling [generate] if necessary.
     */
    suspend fun get(key: Key): Value = cachedItems[key]?.value?.also { moveToTail(key) } ?: cache(key)

    private suspend fun moveToTail(key: Key) {
        mutex.withLock {
            cachedKeys.remove(key)
            cachedKeys.add(key)
        }
    }

    private suspend fun cache(key: Key): Value = coroutineScope {
        val deferred = mutex.withLock {
            itemsInGeneration[key] ?: generateValueAsync(key, this).also { itemsInGeneration[key] = it }
        }
        try {
            deferred.await()
        } finally {
            @Suppress("DeferredResultUnused")
            mutex.withLock {
                itemsInGeneration.remove(key)
            }
        }
    }

    private suspend fun generateValueAsync(key: Key, scope: CoroutineScope): Deferred<Value> {
        return scope.async(Dispatchers.IO) {
            val value = generate(key)

            mutex.withLock {
                cachedItems[key] = Item(value)
                cachedKeys.add(key)
                if (cachedKeys.size > maxItems)
                    evictHead()
                value
            }
        }
    }

    private suspend fun evictHead() {
        cachedItems.remove(cachedKeys.remove())?.let {
            onEvict(it.value)
        }
    }

    suspend fun clear() {
        mutex.withLock {
            itemsInGeneration.clear()
            while (cachedKeys.size > 0)
                evictHead()
        }
    }

    protected fun finalize() {
        runBlocking { clear() }
    }
}
