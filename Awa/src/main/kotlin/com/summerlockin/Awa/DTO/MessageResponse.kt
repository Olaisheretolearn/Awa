package com.summerlockin.Awa.DTO

import com.summerlockin.Awa.model.Reaction
import org.bson.types.ObjectId

data class MessageResponse(
    val id: String,
    val roomId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: String,
    val reactions: List<Reaction> = emptyList(),
    val attachmentUrls: List<String>
)
