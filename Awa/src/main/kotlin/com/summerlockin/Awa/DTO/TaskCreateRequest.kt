package com.summerlockin.Awa.DTO

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.summerlockin.Awa.model.Recurrence

data class TaskCreateRequest(
    val name: String,
    val description: String? = null,
    val assignedTo: String? = null,
    val recurrence: Recurrence = Recurrence.NONE,
    @JsonAlias("nextDueDate", "nextDueDateUtc")
    val nextDueDate: String? = null,
    val icon: String? = null

)
