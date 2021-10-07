package com.eidu.integration.test.app.model.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.eidu.integration.test.app.model.LearningApp

@Dao
interface LearningAppDao {
    @Query("SELECT * FROM learning_apps")
    fun getAll(): LiveData<List<LearningApp>>

    @Query("SELECT * FROM learning_apps WHERE name = :name")
    suspend fun findByName(name: String): LearningApp?

    @Insert(onConflict = REPLACE)
    fun upsert(learningApp: LearningApp)

    @Delete
    fun delete(learningApp: LearningApp)
}
