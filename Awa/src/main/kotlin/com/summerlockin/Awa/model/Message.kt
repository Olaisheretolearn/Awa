package com.summerlockin.Awa.model

import org.apache.logging.log4j.message.TimestampMessage
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import java.time.Instant

data class Message(
    @Id val id: ObjectId? = null,
    val roomId: ObjectId,
    val senderId: ObjectId,
    val senderName: String,
    val content: String,
    val timestamp: Instant = Instant.now(),
    val reactions: MutableList<Reaction> = mutableListOf(),
    val attachmentUrls: List<String> = emptyList()
)
data class Reaction(
    val userId: String,
    val emoji: String
)

