package com.example.raghu.tiger5regulars.utilities

import java.text.SimpleDateFormat
import java.util.*

fun Date.toStringFormat(format: String, locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(format, locale)
    return formatter.format(this)
}

