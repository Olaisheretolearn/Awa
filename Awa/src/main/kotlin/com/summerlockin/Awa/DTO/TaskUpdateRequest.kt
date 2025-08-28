package com.summerlockin.Awa.DTO

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.summerlockin.Awa.model.Recurrence

data class TaskUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val roomId: String? = null,
    val assignedTo: String? = null,
    val recurrence: Recurrence? = null,
    @JsonAlias("nextDueDate", "nextDueDateUtc")
    val nextDueDate: String? = null,
    val icon: String?,
)

