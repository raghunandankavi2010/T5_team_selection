package com.example.raghu.tiger5regulars

import android.content.Context
import android.content.SharedPreferences
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.raghu.tiger5regulars.utilities.PREF_NAME
import com.example.raghu.tiger5regulars.utilities.PRIVATE_MODE
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import java.util.concurrent.TimeUnit

class DailyWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        // Set Execution around 12:00:00 PM
        dueDate.set(Calendar.AM_PM,Calendar.PM)
        dueDate.set(Calendar.HOUR_OF_DAY, 12)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyWorker>()
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag("TAG_OUTPUT")
                .build()
        WorkManager.getInstance(applicationContext)
                .enqueue(dailyWorkRequest)
        val appContext = applicationContext
        val sharedPref: SharedPreferences = appContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        val database = FirebaseDatabase.getInstance().reference
        val id = sharedPref.getString("id",null)
        val ref = id?.let { database.child(it) }
        val playersQuery = ref?.child("Playing")?.setValue(false)
        return Result.success()
    }
}