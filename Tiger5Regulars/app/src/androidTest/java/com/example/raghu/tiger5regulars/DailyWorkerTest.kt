package com.example.raghu.tiger5regulars

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.*
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.raghu.tiger5regulars.ui.DailyWorker
import org.hamcrest.CoreMatchers.`is`
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class DailyWorkerTest {

    @get:Rule
    var wmRule = WorkManagerTestRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun testDaily() {


        // Create request
        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyWorker>()
                .addTag("TAG_OUTPUT")
                .build()

        // Enqueue and wait for result. This also runs the Worker synchronously
        // because we are using a SynchronousExecutor.
        wmRule.workManager.enqueue(dailyWorkRequest).result.get()
        // Get WorkInfo
        val workInfo = wmRule.workManager.getWorkInfoById(dailyWorkRequest.id).get()

        // Assert
       assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))

    }

    @Test
    @Throws(Exception::class)
    fun testWithInitialDelay() {

        // Create request
        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyWorker>()
                .setInitialDelay(10, TimeUnit.MILLISECONDS)
                .addTag("TAG_OUTPUT")
                .build()

        val testDriver = WorkManagerTestInitHelper.getTestDriver(wmRule.testContext)
        // Enqueue and wait for result.
        wmRule.workManager.enqueue(dailyWorkRequest).result.get()
        testDriver?.setInitialDelayMet(dailyWorkRequest.id)
        // Get WorkInfo and outputData
        val workInfo = wmRule.workManager.getWorkInfoById(dailyWorkRequest.id).get()
        // Assert
        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))

    }

    @Test
    @Throws(Exception::class)
    fun testWithConstraints() {

        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        // Create request
        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyWorker>()
                .setConstraints(constraints)
                .build()

        val testDriver = WorkManagerTestInitHelper.getTestDriver(wmRule.testContext)
        // Enqueue and wait for result.
        wmRule.workManager.enqueue(dailyWorkRequest).result.get()
        testDriver?.setAllConstraintsMet(dailyWorkRequest.id)
        // Get WorkInfo and outputData
        val workInfo = wmRule.workManager.getWorkInfoById(dailyWorkRequest.id).get()
        // Assert
        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))

    }

}