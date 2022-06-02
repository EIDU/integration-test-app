package com.eidu.integration.test.app.model

import com.eidu.integration.test.app.util.json
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

@Entity
data class LearningUnit(
    @Index
    val learningAppPackage: String,
    @Index
    val unitId: String,
    val icon: String,
    val fields: Map<String, String>,
    @Convert(converter = TagsConverter::class, dbType = String::class)
    val tags: Tags,
    val additionalAssets: List<String>,
    @Id var id: Long = 0
) {
    fun allowsAsset(filePath: String): Boolean =
        additionalAssets.any {
            filePath == it || (it.endsWith('/') && filePath.startsWith(it))
        }
}

/**
 * This type only exists because ObjectBox appears to have a problem with a property being of
 * type Map<String, Set<String>>, even when specifying a custom converter.
 */
data class Tags(val tags: Map<String, Set<String>>)

class TagsConverter : PropertyConverter<Tags, String> {
    override fun convertToEntityProperty(databaseValue: String?): Tags = Tags(
        if (databaseValue == null)
            emptyMap()
        else
            json.decodeFromString(databaseValue)
    )

    override fun convertToDatabaseValue(entityProperty: Tags?): String =
        json.encodeToString(entityProperty?.tags.orEmpty())
}
