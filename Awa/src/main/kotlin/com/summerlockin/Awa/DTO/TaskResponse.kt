package com.summerlockin.Awa.DTO

import com.fasterxml.jackson.annotation.JsonProperty
import com.summerlockin.Awa.model.Recurrence

data class TaskResponse(
    val id: String,
    val name: String,
    val description: String?,
    val roomId: String,
    val assignedTo: String?,
    val recurrence: Recurrence,

    @JsonProperty("nextDueDateUtc")
    val nextDueDate: String?,
    @JsonProperty("createdDateUtc")
    val createdDate: String,

    @JsonProperty("complete")
    val isComplete: Boolean,

    val iconId: String?,
    val iconImageUrl: String?
)
