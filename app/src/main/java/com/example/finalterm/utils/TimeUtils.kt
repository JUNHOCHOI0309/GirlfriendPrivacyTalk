package com.example.finalterm.utils

import java.text.SimpleDateFormat
import java.util.Locale

fun getCurrentTime(): String {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.format(System.currentTimeMillis())
}

fun getFormattedDateTime() : String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(System.currentTimeMillis())
}