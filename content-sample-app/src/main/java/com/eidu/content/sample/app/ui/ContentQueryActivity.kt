package com.eidu.content.sample.app.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.eidu.content.result.QueryResultData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContentQueryActivity : AppCompatActivity() {

    private val viewModel: ContentQueryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val unitIds = viewModel.getContentUnits().map { unit -> unit.contentUnitId }.toMutableList()
        setResult(RESULT_OK, QueryResultData.fromContentIds(unitIds).toResultIntent())
        finish()
    }
}
