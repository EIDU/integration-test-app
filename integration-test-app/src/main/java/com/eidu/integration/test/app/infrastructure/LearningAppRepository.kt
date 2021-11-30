package com.eidu.integration.test.app.infrastructure

import android.content.Context
import androidx.lifecycle.LiveData
import com.eidu.integration.test.app.model.LearningApp
import com.eidu.integration.test.app.model.LearningApp_
import com.eidu.integration.test.app.model.LearningUnit
import com.eidu.integration.test.app.model.LearningUnit_
import com.eidu.integration.test.app.model.MyObjectBox
import com.eidu.integration.test.app.ui.viewmodel.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.query
import io.objectbox.query.QueryBuilder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LearningAppRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val store = MyObjectBox.builder()
        .androidContext(context.applicationContext)
        .build()

    private val learningApps = store.boxFor<LearningApp>()
    private val learningUnits = store.boxFor<LearningUnit>()

    fun listLive(): LiveData<List<LearningApp>> = ObjectBoxLiveData(learningApps.query { order(LearningApp_.name) })

    fun findByPackageName(packageName: String): LearningApp? =
        learningApps.query { equal(LearningApp_.packageName, packageName, QueryBuilder.StringOrder.CASE_SENSITIVE) }.findFirst()

    fun put(learningApp: LearningApp) {
        learningApps.put(learningApp)
    }

    fun delete(learningApp: LearningApp) {
        learningApps.remove(learningApp)
    }

    fun getLearningUnits(learningAppPackage: String): List<LearningUnit> =
        learningUnits.query { equal(LearningUnit_.learningAppPackage, learningAppPackage, QueryBuilder.StringOrder.CASE_SENSITIVE) }.find()

    fun replaceLearningUnits(learningAppPackage: String, units: List<LearningUnit>) {
        learningUnits.query { equal(LearningUnit_.learningAppPackage, learningAppPackage, QueryBuilder.StringOrder.CASE_SENSITIVE) }.remove()
        learningUnits.put(units)
    }
}
