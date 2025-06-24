package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.TaskResponse
import com.summerlockin.Awa.DTO.TaskCreateRequest
import com.summerlockin.Awa.DTO.TaskUpdateRequest
import com.summerlockin.Awa.exception.NotFoundException
import com.summerlockin.Awa.exception.UnauthorizedException
import com.summerlockin.Awa.model.Recurrence
import com.summerlockin.Awa.model.Task
import com.summerlockin.Awa.repository.TaskRepository
import org.apache.coyote.Response
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
@Service
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


    //get tasks by room
    fun getTasksByRoom(roomId: String): List<TaskResponse> {
        val tasks = taskRepository.findByRoomId(ObjectId(roomId))
        return tasks.map { it.toDTO() }
    }

    //get tasks by user
    fun getTasksByUser(userId: String): List<TaskResponse> {
        val tasks = taskRepository.findByAssignedTo(ObjectId(userId))
        return tasks.map { it.toDTO() }
    }

    // completed tasks
    fun markTaskComplete(taskId: String, userId: String): TaskResponse {
        val task = taskRepository.findById(ObjectId(taskId))
            .orElseThrow { NotFoundException("Task not found") }

        if (task.assignedTo != ObjectId(userId)) {
            throw UnauthorizedException("User not assigned to this task")
        }

        val updated = task.copy(
            isComplete = true
        )

        // Optional: trigger a system message or star increment here
        return taskRepository.save(updated).toDTO()
    }

    //get upcoming task , just incase i ever need this
    fun getUpComingTasks(roomId: String):List<TaskResponse>{
        val now  = Instant.now()
        val tasks = taskRepository.findByRoomIdAndNextDueDateAfter(ObjectId(roomId), now)
        return tasks.map { it.toDTO() }
    }


    fun regenerateRecurringTasks(): List<TaskResponse> {
        val now = Instant.now()
        val dueTasks = taskRepository.findByRecurrenceNotAndNextDueDateBefore(
            Recurrence.NONE,
            now
        )

        val regenerated = dueTasks.map { task ->
            val nextDate = when (task.recurrence) {
                Recurrence.DAILY -> task.nextDueDate?.plus(1, ChronoUnit.DAYS)
                Recurrence.WEEKLY -> task.nextDueDate?.plus(1, ChronoUnit.WEEKS)
                Recurrence.MONTHLY -> task.nextDueDate?.plus(1, ChronoUnit.MONTHS)
                else -> null
            }

            task.copy(
                isComplete = false,
                nextDueDate = nextDate
            )
        }

        return taskRepository.saveAll(regenerated).map { it.toDTO() }
    }


    fun getTasksByRoomAndStatus(roomId: String, isComplete: Boolean): List<TaskResponse> {
        val tasks = taskRepository.findByRoomIdAndIsComplete(ObjectId(roomId), isComplete)
        return tasks.map { it.toDTO() }
    }

    fun getTasksByUserAndStatus(userId: String, isComplete: Boolean): List<TaskResponse> {
        val tasks = taskRepository.findByAssignedToAndIsComplete(ObjectId(userId), isComplete)
        return tasks.map { it.toDTO() }
    }






    fun deleteTask(taskId: String): Boolean {
        val task = taskRepository.findById(ObjectId(taskId))
            .orElseThrow { NotFoundException("Task not found") }

        taskRepository.delete(task)
        return true
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