package com.eidu.content.sample.app.content

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentUnitsDao @Inject constructor() {
    fun getContentUnits(): List<ContentUnit> = (1..20).map {
        ContentUnit("Sample_Content_Unit_$it")
    }
}
