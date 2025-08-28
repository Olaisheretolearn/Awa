package com.summerlockin.Awa.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("rooms")
data class Room(
    @Id val id: ObjectId? = null,
    val name: String,
    val code: String,
    val ownerId: ObjectId,
    val createdAt: Instant = Instant.now(),
    val city: String? = null,

    )
