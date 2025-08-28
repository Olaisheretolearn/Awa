package com.summerlockin.Awa.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("tasks")
data class Task(
    @Id val id: ObjectId? = null,
    val name: String,
    val description: String? = null,
    val roomId: ObjectId,
    val assignedTo: ObjectId? = null,
    val recurrence: Recurrence = Recurrence.NONE,
    val nextDueDate: Instant?,
    @CreatedDate
    val createdDate: Instant = Instant.now(),
    val isComplete: Boolean = false,
    val icon: TaskIcon? = null
)



enum class Recurrence {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY
}
