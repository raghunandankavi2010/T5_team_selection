package com.example.raghu.tiger5regulars.models

import com.google.firebase.database.Exclude

data class User(
        var username: String? = "",
        var playing: Boolean = false,
        var today: String? = ""
){
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
                "Name" to username,
                "Playing" to playing,
                "Today" to today
        )
    }
}