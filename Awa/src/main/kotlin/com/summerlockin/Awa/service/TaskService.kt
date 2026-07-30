package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.TaskResponse
import com.summerlockin.Awa.DTO.TaskCreateRequest
import com.summerlockin.Awa.DTO.TaskUpdateRequest
import com.summerlockin.Awa.exception.NotFoundException
import com.summerlockin.Awa.exception.UnauthorizedException
import com.summerlockin.Awa.model.Recurrence
import com.summerlockin.Awa.model.Task
import com.summerlockin.Awa.model.TaskIcon
import com.summerlockin.Awa.repository.TaskRepository
import org.bson.types.ObjectId
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
@Service
class TaskService(
     private  val taskRepository: TaskRepository,
     private val authorizationService: AuthorizationService
) {

    fun createTask(roomId: String, actingUserId: String, request: TaskCreateRequest): TaskResponse {
        authorizationService.requireRoomMember(roomId, actingUserId)
        request.assignedTo?.let { authorizationService.requireRoomMember(roomId, it) }
        val task = Task(
            name = request.name,
            description = request.description,
            roomId = ObjectId(roomId),
            assignedTo = request.assignedTo?.let { ObjectId(it) },
            recurrence = request.recurrence,
            nextDueDate = request.nextDueDate?.let { Instant.parse(it) },
            icon = parseIconOrNull(request.icon) ?: TaskIcon.CLEANING,
            isComplete = false

        )
        return taskRepository.save(task).toDTO()
    }

    private fun parseIconOrNull(id: String?): TaskIcon? =
        id?.let { TaskIcon.valueOf(it) }


    fun updateTask(roomId: String, taskId: String, actingUserId: String, request: TaskUpdateRequest): TaskResponse {
        val task = taskRepository.findById(ObjectId(taskId))
            .orElseThrow { NotFoundException("Task not found") }

        authorizationService.requireRoomMember(roomId, actingUserId)
        if (task.roomId != ObjectId(roomId)) {
            throw NotFoundException("Task not found")
        }
        authorizationService.requireTaskRoomAccess(task, actingUserId)
        authorizationService.requireTaskOwnerOrAssignee(task, actingUserId)

        val updated = task.copy(
            name = request.name ?: task.name,
            description = request.description ?: task.description,
            roomId = task.roomId,
            assignedTo = request.assignedTo?.let { ObjectId(it) } ?: task.assignedTo,
            recurrence = request.recurrence ?: task.recurrence,
            nextDueDate = request.nextDueDate?.let {
                try { Instant.parse(it) }
                catch (_: DateTimeParseException) { throw IllegalArgumentException("Invalid date format for nextDueDateUtc") }
            } ?: task.nextDueDate,
            icon = request.icon?.let { TaskIcon.valueOf(it) } ?: task.icon
        )

        return taskRepository.save(updated).toDTO()
    }

    fun getTaskById(roomId: String, taskId: String, actingUserId: String): TaskResponse {
        val task = taskRepository.findById(ObjectId(taskId))
            .orElseThrow { NotFoundException("Task not found") }
        authorizationService.requireRoomMember(roomId, actingUserId)
        if (task.roomId != ObjectId(roomId)) {
            throw NotFoundException("Task not found")
        }
        authorizationService.requireTaskRoomAccess(task, actingUserId)
        return task.toDTO()
    }


    //get tasks by room
    fun getTasksByRoom(roomId: String, actingUserId: String): List<TaskResponse> {
        authorizationService.requireRoomMember(roomId, actingUserId)
        val tasks = taskRepository.findByRoomId(ObjectId(roomId))
        return tasks.map { it.toDTO() }
    }

    //get tasks by user
    fun getTasksByUser(userId: String, actingUserId: String): List<TaskResponse> {
        authorizationService.requireSelf(actingUserId, userId)
        val tasks = taskRepository.findByAssignedTo(ObjectId(userId))
        return tasks.map { it.toDTO() }
    }

    // completed tasks
    fun markTaskComplete(roomId: String, taskId: String, userId: String): TaskResponse {
        val task = taskRepository.findById(ObjectId(taskId))
            .orElseThrow { NotFoundException("Task not found") }

        authorizationService.requireRoomMember(roomId, userId)
        if (task.roomId != ObjectId(roomId)) {
            throw NotFoundException("Task not found")
        }
        authorizationService.requireTaskRoomAccess(task, userId)
        if (task.assignedTo != ObjectId(userId)) {
            throw AccessDeniedException("User not assigned to this task")
        }

        val updated = task.copy(
            isComplete = true
        )

        // Optional: trigger a system message or star increment here
        return taskRepository.save(updated).toDTO()
    }

    //get upcoming task , just incase i ever need this
    fun getUpComingTasks(roomId: String, actingUserId: String):List<TaskResponse>{
        authorizationService.requireRoomMember(roomId, actingUserId)
        val now  = Instant.now()
        val tasks = taskRepository.findByRoomIdAndNextDueDateAfter(ObjectId(roomId), now)
        return tasks.map { it.toDTO() }
    }


    fun regenerateRecurringTasks(roomId: String, actingUserId: String): List<TaskResponse> {
        authorizationService.requireRoomOwner(roomId, actingUserId)
        val now = Instant.now()
        val due = taskRepository.findByRoomId(ObjectId(roomId))
            .filter { it.recurrence != Recurrence.NONE && it.nextDueDate != null && it.nextDueDate.isBefore(now) }

        val regenerated = due.map { task ->
            val next = when (task.recurrence) {
                Recurrence.DAILY -> task.nextDueDate?.plus(1, ChronoUnit.DAYS)
                Recurrence.WEEKLY -> task.nextDueDate?.plus(1, ChronoUnit.WEEKS)
                Recurrence.MONTHLY -> task.nextDueDate?.plus(1, ChronoUnit.MONTHS)
                Recurrence.NONE -> task.nextDueDate
            }
            task.copy(isComplete = false, nextDueDate = next)
        }

        return taskRepository.saveAll(regenerated).map { it.toDTO() }
    }



    fun getTasksByRoomAndStatus(roomId: String, actingUserId: String, isComplete: Boolean): List<TaskResponse> {
        authorizationService.requireRoomMember(roomId, actingUserId)
        val tasks = taskRepository.findByRoomIdAndIsComplete(ObjectId(roomId), isComplete)
        return tasks.map { it.toDTO() }
    }

    fun getTasksByUserAndStatus(userId: String, actingUserId: String, isComplete: Boolean): List<TaskResponse> {
        authorizationService.requireSelf(actingUserId, userId)
        val tasks = taskRepository.findByAssignedToAndIsComplete(ObjectId(userId), isComplete)
        return tasks.map { it.toDTO() }
    }






    fun deleteTask(roomId: String, taskId: String, actingUserId: String): Boolean {
        val task = taskRepository.findById(ObjectId(taskId))
            .orElseThrow { NotFoundException("Task not found") }

        authorizationService.requireRoomMember(roomId, actingUserId)
        if (task.roomId != ObjectId(roomId)) {
            throw NotFoundException("Task not found")
        }
        authorizationService.requireTaskRoomAccess(task, actingUserId)
        authorizationService.requireTaskOwnerOrAssignee(task, actingUserId)

        taskRepository.delete(task)
        return true
    }


    private fun Instant.toIso(): String = this.toString()

    private fun Task.toDTO(): TaskResponse =
        TaskResponse(
            id = this.id?.toHexString() ?: "",
            name = this.name,
            description = this.description,
            roomId = this.roomId.toHexString(),
            assignedTo = this.assignedTo?.toHexString(),
            recurrence = this.recurrence,
            nextDueDate = this.nextDueDate?.toIso(),
            createdDate = this.createdDate.toIso(),
            isComplete = this.isComplete,
            iconId = this.icon?.name,
            iconImageUrl = this.icon?.imageUrl
        )

}