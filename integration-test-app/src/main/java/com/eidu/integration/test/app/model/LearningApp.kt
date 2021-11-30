package com.eidu.integration.test.app.model

import io.objectbox.annotation.ConflictStrategy
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique

@Entity
data class LearningApp(
    val name: String,
    @Unique(onConflict = ConflictStrategy.REPLACE) val packageName: String,
    val launchClass: String,
    @Id var id: Long = 0
)
