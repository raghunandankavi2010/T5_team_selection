package com.example.raghu.tiger5regulars


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.raghu.tiger5regulars.models.User
import com.example.raghu.tiger5regulars.utilities.PREF_NAME
import com.example.raghu.tiger5regulars.utilities.PRIVATE_MODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home.*
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
            startActivity(intent)
            finish()
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

            val userId = sharedPref.getString("id", null)
            if (userId != null) {
                val isUserPlaying = database.child("Players").child(userId)
                isUserPlaying.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        progressBar.visibility = View.GONE
                        val user = dataSnapshot.getValue(User::class.java)
                        user?.let {
                            switch_btn.visibility = View.VISIBLE
                            switch_btn.setChecked(checked = user.Playing, alsoNotify = false)
                            if (user.Playing) {
                                switch_btn.text = getString(R.string.joined)
                            } else {
                                switch_btn.text = getString(R.string.notjoining)
                            }
                        }
                        isUserPlaying.removeEventListener(this)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w("HomeActivtiy", "loadPost:onCancelled", databaseError.toException())
                    }
                })
            }
            val constraints = Constraints.Builder()
                    .setRequiresCharging(true)
                    .build()

            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance()
            dueDate.set(Calendar.AM_PM, Calendar.PM)
            dueDate.set(Calendar.HOUR_OF_DAY, 12)
            dueDate.set(Calendar.MINUTE, 0)
            dueDate.set(Calendar.SECOND, 0)
            if (dueDate.before(currentDate)) {
                dueDate.add(Calendar.HOUR_OF_DAY, 24)
            }
            val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
            val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyWorker>()
                    .setConstraints(constraints)
                    .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                    .addTag("TAG_OUTPUT")
                    .build()
            WorkManager.getInstance(applicationContext).enqueue(dailyWorkRequest)
            switch_btn.setOnCheckedChangeListener { _, isChecked ->
                    checkCount(isChecked)
            }
            viewTeam.setOnClickListener {
                startActivity(Intent(this@HomeActivity, MainActivity::class.java))
            }
        }

    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id: Int = item.itemId
        if (id == R.id.action_logout) {
            auth.signOut()
            val intent = Intent(this@HomeActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun update(name: String?, playing: Boolean, uid: String?) {

        val objRef = database.child("Players")
        uid?.let {
            objRef.child(it).child("Playing").setValue(playing)
            objRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
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
                Log.i("HomeActivity",""+count)
                if (isChecked && count < 10) {
                    update(sharedPref.getString(PREF_NAME, null), isChecked, sharedPref.getString("id", null))
                    switch_btn.setChecked(checked = true, alsoNotify = false)
                    switch_btn.text = getString(R.string.joined)
                    playersQuery.removeEventListener(this)
                } else if (!isChecked) {
                    switch_btn.setChecked(checked = false, alsoNotify = false)
                    update(sharedPref.getString(PREF_NAME, null), isChecked, sharedPref.getString("id", null))
                    switch_btn.text = getString(R.string.notjoining)
                    playersQuery.removeEventListener(this)
                }else{
                    switch_btn.setChecked(checked = false, alsoNotify = false)
                    playersQuery.removeEventListener(this)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("HomeActivity", "loadPost:onCancelled", databaseError.toException())
            }
        })
    }
}
