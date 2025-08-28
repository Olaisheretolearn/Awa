package com.summerlockin.Awa.DTO

import java.time.Instant

data class BillResponse(
    val id: String,
    val roomId: String,
    val name: String,
    val description: String,
    val amount: Double,
    val dueDate: Instant,
    val paidByUserId: String,
    val isPaid: Boolean,
    val splitAmongUserIds: List<String>,
    val shares: List<BillShareResponse>,
    val totalOwedToCreator: Double
)