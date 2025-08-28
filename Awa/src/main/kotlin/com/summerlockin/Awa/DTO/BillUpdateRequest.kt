package com.summerlockin.Awa.DTO

import java.time.Instant

data class BillUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val amount: Double? = null,
    val dueDate: Instant? = null,
    val splitAmongUserIds: List<String>? = null // re-split not supported here (keep simple)
)