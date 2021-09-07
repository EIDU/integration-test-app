package com.eidu.content.sample.app.ui

import androidx.lifecycle.ViewModel
import com.eidu.content.sample.app.content.ContentUnit
import com.eidu.content.sample.app.content.ContentUnitsDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ContentQueryViewModel @Inject constructor(
    private val contentUnitsDao: ContentUnitsDao
) : ViewModel() {
    fun getContentUnits(): List<ContentUnit> =
        contentUnitsDao.getContentUnits()
}
