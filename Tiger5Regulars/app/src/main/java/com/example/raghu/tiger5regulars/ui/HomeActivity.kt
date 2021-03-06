package com.example.raghu.tiger5regulars.ui


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.example.raghu.tiger5regulars.R
import com.example.raghu.tiger5regulars.models.User
import com.example.raghu.tiger5regulars.utilities.PREF_NAME
import com.example.raghu.tiger5regulars.utilities.PRIVATE_MODE
import com.example.raghu.tiger5regulars.utilities.getCurrentDateTime
import com.example.raghu.tiger5regulars.utilities.toStringFormat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit


class HomeActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var count = 0
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            val intent = Intent(this@HomeActivity, LoginActivity::class.java)
            intent.apply {
                startActivity(this)
                finish()
            }
        } else {
            setContentView(R.layout.activity_home)
            setSupportActionBar(toolbar)

            supportActionBar?.let {
                title = "Ready to Play?"
                it.setDisplayHomeAsUpEnabled(true)
                it.setDisplayShowHomeEnabled(true)
                it.setHomeAsUpIndicator(R.drawable.ic_home)
            }

            sharedPref = getSharedPreferences(PREF_NAME, PRIVATE_MODE)

            database = FirebaseDatabase.getInstance().reference

            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance()
            dueDate.set(Calendar.HOUR_OF_DAY, 1)
            dueDate.set(Calendar.MINUTE,0)
            dueDate.set(Calendar.SECOND, 0)
            if (dueDate.before(currentDate)) {
                dueDate.add(Calendar.HOUR_OF_DAY, 24)
                Timber.i("Due date before current date ")
            }
            val timeDiff = dueDate.timeInMillis  - currentDate.timeInMillis
            Timber.i("Time Diff $timeDiff ")
            val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyWorker>()
                    .setConstraints(constraints)
                    .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                    .addTag("TAG_OUTPUT")
                    .build()
            WorkManager.getInstance(applicationContext)
                    .enqueueUniqueWork("DailyWork", ExistingWorkPolicy.REPLACE,dailyWorkRequest)
            WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(dailyWorkRequest.id)
                    .observe(this@HomeActivity, androidx.lifecycle.Observer { workInfo ->
                        if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                            Timber.i("WorkManger Finished")
                        }else if(workInfo != null && workInfo.state == WorkInfo.State.ENQUEUED) {
                            Timber.i("WorkManger Enqueued")
                        }else if(workInfo != null && workInfo.state == WorkInfo.State.RUNNING) {
                            Timber.i("WorkManger Running")
                        }
                    })
            switch_btn.setOnClickListener { view ->
                if (view is CheckBox) {
                    val isChecked = view.isChecked
                    checkCount(isChecked)
                }

            }
            viewTeam.setOnClickListener {
               val intent = Intent(this@HomeActivity, MainActivity::class.java)
                intent.apply {
                    startActivity(this)
                }

            }

            map.setOnClickListener {
                val intent = Intent(this@HomeActivity,MapsActivity::class.java)
                intent.apply {
                    startActivity(this)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = sharedPref.getString("id", null)
        if (userId != null) {
            val isUserPlaying = database.child("Players").child(userId)
            isUserPlaying.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    progressBar.visibility = View.GONE
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let {
                        switch_btn.visibility = View.VISIBLE
                        if (user.Playing) {
                            switch_btn.isChecked = true
                            switch_btn.text = getString(R.string.joined)
                        } else {
                            switch_btn.isChecked = false
                            switch_btn.text = getString(R.string.notjoining)
                        }
                    }
                    isUserPlaying.removeEventListener(this)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    isUserPlaying.removeEventListener(this)
                    Timber.w(databaseError.toException(), "loadPost:onCancelled")
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
             R.id.action_logout -> {
                 auth.signOut()
                 val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                 startActivity(intent)
                 finish()
                 return true
             }
            R.id.action_settings -> {
                val intent = Intent(this@HomeActivity, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun update(name: String?, playing: Boolean, uid: String?) {

        val objRef = database.child("Players")
        uid?.let {
            objRef.child(it).child("Playing").setValue(playing)
            val date = getCurrentDateTime()
            val dateInString = date.toStringFormat("dd/MM/yyyy")
            objRef.child(it).child("Today").setValue(dateInString)
            objRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    Timber.w(databaseError.toException(), "loadPost:onCancelled")
                    objRef.removeEventListener(this)
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    objRef.removeEventListener(this)
                }

            })
        }
    }

    private fun checkCount(isChecked: Boolean) {
        val ref = database.child("Players")
        val playersQuery = ref.orderByChild("Playing").equalTo(true)
        playersQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                count = dataSnapshot.children.count()
                Timber.i("Count =$count")
                if (isChecked && count < 10) {
                    update(sharedPref.getString(PREF_NAME, null), isChecked, sharedPref.getString("id", null))
                    switch_btn.text = getString(R.string.joined)
                    playersQuery.removeEventListener(this)
                } else if (!isChecked) {
                    update(sharedPref.getString(PREF_NAME, null), isChecked, sharedPref.getString("id", null))
                    switch_btn.text = getString(R.string.notjoining)
                    playersQuery.removeEventListener(this)
                }else if(count==10){
                    Snackbar.make(root,"Already 10 players have joined!. Try again later if someone cancels his spot",Snackbar.LENGTH_LONG).show()
                    switch_btn.isChecked = false
                    playersQuery.removeEventListener(this)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.tag("HomeActivity").w(databaseError.toException(), "loadPost:onCancelled")
                playersQuery.removeEventListener(this)
            }
        })
    }

}
