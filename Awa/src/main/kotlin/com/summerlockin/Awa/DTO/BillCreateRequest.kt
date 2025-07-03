package com.summerlockin.Awa.DTO

import java.time.Instant

data class BillCreateRequest(
    val roomId: String,
    val name: String,
    val description: String,
    val amount: Double,
    val dueDate: Instant,
    val paidByUserId: String? = null,
    val isPaid: Boolean = false,
    val splitAmongUserIds: List<String>
)
