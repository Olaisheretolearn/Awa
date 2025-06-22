package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.TaskResponse
import com.summerlockin.Awa.DTO.TaskCreateRequest
import com.summerlockin.Awa.model.Task
import com.summerlockin.Awa.repository.TaskRepository
import org.apache.coyote.Response
import org.bson.types.ObjectId
import java.time.Instant

class TaskService(
    private  val taskRepository: TaskRepository
) {

    fun createTask(request: TaskCreateRequest): TaskResponse {
        val task = Task(
            name = request.name,
            description = request.description,
            roomId = ObjectId(request.roomId),
            // made an error of not making assignedTo nullable . fixed
            assignedTo = request.assignedTo?.let { ObjectId(it) },
            recurrence = request.recurrence,
            //also made an error of not parsing it
            nextDueDate = request.nextDueDate?.let { Instant.parse(it) }
        )
        return taskRepository.save(task).toDTO()
    }



    fun Task.toDTO(): TaskResponse {
        return TaskResponse(
            id = this.id.toString(),
            name = this.name,
            description = this.description,
            roomId = this.roomId.toString(),
            assignedTo = this.assignedTo?.toString(),
            recurrence = this.recurrence,
            nextDueDate = this.nextDueDate?.toString(),
            createdDate = this.createdDate.toString(),
            isComplete = this.isComplete
        )
    }

}