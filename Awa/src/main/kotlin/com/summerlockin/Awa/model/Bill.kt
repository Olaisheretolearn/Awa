package com.summerlockin.Awa.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("bills")
data class Bill(
    @Id val id: ObjectId? = null,
    val roomId: ObjectId,
    val amount: Double,
    val name: String,
    val description: String,
    val dueDate: Instant,
    val paidBy: ObjectId,               // Who paid the bill
    val isPaid: Boolean = false,
    val splitAmong: List<ObjectId>,
    val createdAt: Instant = Instant.now()
)
