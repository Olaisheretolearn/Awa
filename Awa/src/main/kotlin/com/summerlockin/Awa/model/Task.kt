package com.summerlockin.Awa.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import java.time.Instant

@Document("tasks")
data class Task(
    @Id val id: ObjectId? = null,
    val name: String,
    val description: String? = null,
    val roomId: ObjectId,
    val assignedTo: ObjectId? = null,
    val recurrence: Recurrence = Recurrence.NONE,
    val nextDueDate: Instant? = null,
    val createdDate: Instant = Instant.now(),
    val isComplete: Boolean = false
)



enum class Recurrence {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY
}
