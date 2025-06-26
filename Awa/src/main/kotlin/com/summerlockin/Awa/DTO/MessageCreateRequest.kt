package com.summerlockin.Awa.DTO

import org.bson.types.ObjectId

data class MessageCreateRequest (
    val content: String,
    val senderId: String,
    val senderName: String,
    val roomId: String,
    val attachmentUrls: List<String> = emptyList()
)
