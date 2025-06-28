package com.summerlockin.Awa.DTO

import com.summerlockin.Awa.model.Recurrence

data class TaskUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val roomId: String? = null,
    val assignedTo: String? = null,
    val recurrence: Recurrence? = null,
    val nextDueDate: String? = null
)

