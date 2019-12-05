package com.example.raghu.tiger5regulars.models

import com.google.firebase.database.Exclude

data class User(
        var Name: String? = "",
        var Playing: Boolean = false,
        var Today: String? = ""
){
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
                "Name" to Name,
                "Playing" to Playing,
                "Today" to Today
        )
    }
}