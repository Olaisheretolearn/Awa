package com.summerlockin.Awa.DTO

import com.summerlockin.Awa.model.Recurrence

data class TaskResponse(
    val id: String,
    val name: String,
    val description: String?,
    val roomId: String,
    val assignedTo: String?,
    val recurrence: Recurrence,
    val nextDueDate: String?,
    val createdDate: String,
    val isComplete: Boolean
)
