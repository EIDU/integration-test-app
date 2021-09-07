package com.eidu.content.test.app.model

data class ContentUnit(
    val contentApp: ContentApp,
    val contentAppVersion: String,
    val unitId: String,
    val querySource: QuerySource
)

enum class QuerySource {
    ContentProvider,
    Intent
}
