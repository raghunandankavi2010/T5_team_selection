package com.example.raghu.tiger5regulars.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.work.*
import com.example.raghu.tiger5regulars.utilities.PREF_NAME
import com.example.raghu.tiger5regulars.utilities.PRIVATE_MODE
import com.google.firebase.database.FirebaseDatabase
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class DailyWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        Timber.i("WorkManger doing work")
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        // Set Execution around 12:00:00 PM
        dueDate.set(Calendar.AM_PM,Calendar.PM)
        dueDate.set(Calendar.HOUR_OF_DAY, 1)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
            Timber.i("Due date before current date ")
        }
        val timeDiff = dueDate.timeInMillis  - currentDate.timeInMillis
        Timber.i("Time Diff $timeDiff ")
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyWorker>()
                .setConstraints(constraints)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag("TAG_OUTPUT")
                .build()
        WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork("DailyWork",ExistingWorkPolicy.REPLACE,dailyWorkRequest)
        val appContext = applicationContext
        val sharedPref: SharedPreferences = appContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        val database = FirebaseDatabase.getInstance().reference
        val id = sharedPref.getString("id",null)
        Timber.i("User Id: {$id}")
        id?.let {
            val objRef = database.child("Players").child(it)
           objRef.child("Playing").setValue(false)
        }

        return Result.success()
    }
}