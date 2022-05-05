package com.eidu.integration.test.app.util

import java.util.zip.ZipInputStream

fun ZipInputStream.fileEntries() =
    generateSequence(::getNextEntry)
        .filter { entry ->
            !entry.isDirectory &&
                !entry.name.split('/').any { it.startsWith('.') || it.isBlank() }
        }
