package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.TaskResponse
import com.summerlockin.Awa.DTO.TaskCreateRequest
import com.summerlockin.Awa.DTO.TaskUpdateRequest
import com.summerlockin.Awa.exception.NotFoundException
import com.summerlockin.Awa.model.Task
import com.summerlockin.Awa.repository.TaskRepository
import org.apache.coyote.Response
import org.bson.types.ObjectId
import java.time.Instant
import java.time.format.DateTimeParseException

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

    fun updateTask(taskId: String ,request: TaskUpdateRequest): TaskResponse{
        val task = taskRepository.findById(ObjectId(taskId))
            .orElseThrow {
                RuntimeException("Task not found")
            }

        val updated = task.copy(
            name = request.name ?: task.name,
            description = request.description ?: task.description,
            roomId = request.roomId?.let{ ObjectId(it)} ?: task.roomId,
            assignedTo =  request.assignedTo?.let { ObjectId(it) } ?: task.assignedTo,
            recurrence = request.recurrence ?: task.recurrence,
            nextDueDate = request.nextDueDate?.let {
                try {
                    Instant.parse(it)
                } catch (e: DateTimeParseException) {
                    throw IllegalArgumentException("Invalid date format for nextDueDate")
                }
            } ?: task.nextDueDate
        )

        return taskRepository.save(updated).toDTO()
    }

    fun getTaskById(taskId: String): TaskResponse{
        val task = taskRepository.findById(ObjectId(taskId))
            .orElseThrow{
                NotFoundException("Task not found")
            }
        return task.toDTO()
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