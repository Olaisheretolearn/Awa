// com.summerlockin.Awa.model
package com.summerlockin.Awa.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.math.BigDecimal
import java.math.RoundingMode

enum class ShareStatus { PENDING, MARKED_PAID, CONFIRMED }

data class BillShare(
    val userId: ObjectId,
    val amount: Double,
    val status: ShareStatus = ShareStatus.PENDING,
    val markedPaidAt: Instant? = null,
    val confirmedPaidAt: Instant? = null
)

@Document("bills")
data class Bill(
    @Id val id: ObjectId? = null,
    val roomId: ObjectId,
    val amount: Double,
    val name: String,
    val description: String,
    val dueDate: Instant,
    val paidBy: ObjectId,                       // creator = payer
    val isPaid: Boolean = false,                // true when all shares CONFIRMED
    val splitAmong: List<ObjectId>,             // participants (can include payer; we’ll strip it)
    val shares: MutableList<BillShare> = mutableListOf(), // <- per-person owed to creator
    val createdAt: Instant = Instant.now()
)
