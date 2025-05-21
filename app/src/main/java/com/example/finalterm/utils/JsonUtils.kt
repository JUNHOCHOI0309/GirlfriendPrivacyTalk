package com.example.finalterm.utils

import com.example.finalterm.recycleView.MessageData
import com.google.gson.Gson


fun convertMessagesToJson(messages: List<MessageData>): String {
    return Gson().toJson(messages)
}

fun convertJsonToMessages(json: String): List<MessageData> {
    return Gson().fromJson(json, Array<MessageData>::class.java).toList()
}

