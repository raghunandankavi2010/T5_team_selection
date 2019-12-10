package com.example.raghu.tiger5regulars.models

import com.google.firebase.database.Exclude

data class UserProfile(
var Name: String? = "",
var Email: String? ="",
var Number: String? = "",
var Photo:String? =""
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
                "Name" to Name,
                "Email" to Email,
                "Number" to Number,
                "Photo" to Photo
        )
    }
}