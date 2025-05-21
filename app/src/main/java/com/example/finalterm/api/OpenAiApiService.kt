package com.example.finalterm.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface  OpenAiApiService {
    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer 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"
    )
    @POST("v1/chat/completions")
    fun getChatCompletion(@Body request: ChatRequest): Call<ChatResponse>
}

data class ChatRequest(
    val model:String = "gpt-4",
    val messages: List<Message>,
    val max_tokens: Int = 150,
    val temperature: Double = 0.7,
    val top_p: Double = 1.0
)

data class Message(
    val role: String,
    val content: String
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)