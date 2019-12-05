package com.example.raghu.tiger5regulars.utilities

import com.google.firebase.database.DataSnapshot


interface Listener {

    fun onSuccess(dataSnapshot: DataSnapshot?)

}