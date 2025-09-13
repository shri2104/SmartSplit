package com.example.smartsplit.Data

data class ChatRequest(
    val model: String = "gpt-4",
    val messages: List<Message>,
    val max_tokens: Int = 200
)

data class Message(
    val role: String, // "system", "user", "assistant"
    val content: String
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
