package com.summerlockin.Awa.DTO

data class MessageCreateRequest (
    val content: String,
    val roomId: String,
    val attachmentUrls: List<String> = emptyList()
)
