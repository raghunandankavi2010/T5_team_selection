package com.example.raghu.tiger5regulars


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*
import java.util.concurrent.TimeUnit


class HomeActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private val PREF_NAME = "login"
    private var PRIVATE_MODE = 0
    private var count = 0
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if(account==null) {
            val intent = Intent(this@HomeActivity,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }else {
            setContentView(R.layout.activity_home)
            auth = FirebaseAuth.getInstance()
            val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
            database = FirebaseDatabase.getInstance().reference

            val constraints = Constraints.Builder()
                    .setRequiresCharging(true)
                    .build()

            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance()
            dueDate.set(Calendar.AM_PM,Calendar.PM)
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
            checkCount()
            switch_btn.visibility = View.GONE
            switch_btn.setOnCheckedChangeListener { _, isChecked ->
                if(count in 1..10){
                    update(sharedPref.getString(PREF_NAME,null),isChecked,sharedPref.getString("id",null))
                }else{
                    if(!isChecked) {
                        switch_btn.isChecked = false
                        update(sharedPref.getString(PREF_NAME,null),isChecked,sharedPref.getString("id",null))

                    }else if(isChecked && count==0){
                        update(sharedPref.getString(PREF_NAME,null),isChecked,sharedPref.getString("id",null))

                    }else{
                        Snackbar.make(root, "Cannot include already 10 people are in", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id: Int = item.getItemId()
        if (id == R.id.action_logout) {
            auth.signOut()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun update(name:String?,playing:Boolean,uid:String?){

        val objRef = database.child("Players")
        uid?.let {
         objRef.child(it).child("Playing").setValue(playing)
            startActivity(Intent(this@HomeActivity,MainActivity::class.java))
        }
    }

    private fun checkCount() {
        val ref = database.child("Players")
        val playersQuery = ref.orderByChild("Playing").equalTo(true)


        playersQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var count = 0
                    for (players in dataSnapshot.children) {
                        count++
                    }
                if(count in 1..10 || count==0){
                    switch_btn.visibility = View.VISIBLE
                }
                }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("HomeActivity", "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

}
