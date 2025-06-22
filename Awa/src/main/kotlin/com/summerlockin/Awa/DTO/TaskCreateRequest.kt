package com.summerlockin.Awa.DTO

import com.summerlockin.Awa.model.Recurrence

data class TaskCreateRequest(
    val name: String,
    val description: String? = null,
    val roomId: String,
    val assignedTo: String? = null,
    val recurrence: Recurrence = Recurrence.NONE,
    val nextDueDate: String? = null
)
