package com.example.raghu.tiger5regulars

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.raghu.tiger5regulars.models.UserProfile
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_user_details.*


class UserDetails : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            title = "Player Details"
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
            database = FirebaseDatabase.getInstance().reference
            val ref = database.child("PlayersProfile")
            val profileQuery = ref.child(intent.getStringExtra("userid")!!)
            val radius = resources.getDimensionPixelSize(R.dimen.profile_corner_radius)
            profileQuery.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userProfile = dataSnapshot.getValue(UserProfile::class.java)
                    userProfile?.let {
                        Glide.with(this@UserDetails)
                                .load(it.Photo)
                                .transform(CenterCrop(), RoundedCorners(radius))
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .transition(DrawableTransitionOptions.withCrossFade()).into(imageView)
                        name.text = it.Name
                        email.text = it.Email
                        number.text = it.Number
                    }
                }
            })
        }

    }

