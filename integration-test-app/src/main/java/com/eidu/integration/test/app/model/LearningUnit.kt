package com.eidu.integration.test.app.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class LearningUnit(
    @Index
    val learningAppPackage: String,
    @Index
    val unitId: String,
    val icon: String,
    val additionalAssets: List<String>,
    @Id var id: Long = 0
) {
    fun allowsAsset(filePath: String): Boolean =
        additionalAssets.any {
            filePath == it || (it.endsWith('/') && filePath.startsWith(it))
        }
}
