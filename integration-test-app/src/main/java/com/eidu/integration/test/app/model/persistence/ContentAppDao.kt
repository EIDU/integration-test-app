package com.eidu.integration.test.app.model.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.eidu.integration.test.app.model.ContentApp

@Dao
interface ContentAppDao {
    @Query("SELECT * FROM content_apps")
    fun getAll(): LiveData<List<ContentApp>>

    @Query("SELECT * FROM content_apps WHERE name = :name")
    suspend fun findByName(name: String): ContentApp?

    @Insert(onConflict = REPLACE)
    fun upsert(contentApp: ContentApp)

    @Delete
    fun delete(contentApp: ContentApp)
}
