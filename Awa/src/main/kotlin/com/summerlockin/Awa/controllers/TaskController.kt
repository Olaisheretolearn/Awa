package com.summerlockin.Awa.controllers

import com.summerlockin.Awa.DTO.TaskCreateRequest
import com.summerlockin.Awa.DTO.TaskResponse
import com.summerlockin.Awa.DTO.TaskUpdateRequest
import com.summerlockin.Awa.security.UserPrincipal
import com.summerlockin.Awa.service.TaskService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.core.annotation.AuthenticationPrincipal

@Suppress("UNUSED_PARAMETER")
@RestController
@RequestMapping("/api/room/{roomId}/task")
class TaskController(
    private val taskService: TaskService
) {

    @PostMapping
    fun createTask(
        @PathVariable roomId: String,
        @RequestBody req: TaskCreateRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<TaskResponse> =
        ResponseEntity.status(201).body(taskService.createTask(roomId, principal.getId(), req))




    @PatchMapping("/{taskId}")
    @Suppress("UNUSED_PARAMETER")
    fun updateTask(
        @PathVariable roomId: String,
        @PathVariable taskId: String,
        @RequestBody request: TaskUpdateRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<TaskResponse> {
        val updatedTask = taskService.updateTask(roomId, taskId, principal.getId(), request)
        return ResponseEntity.ok(updatedTask)
    }

    @GetMapping("/{taskId}")
    @Suppress("UNUSED_PARAMETER")
    fun getTask(
        @PathVariable roomId: String,
        @PathVariable taskId: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<TaskResponse> {
        val task = taskService.getTaskById(roomId, taskId, principal.getId())
        return ResponseEntity.ok(task)
    }

    @GetMapping
    fun getTasksByRoom(@PathVariable roomId: String, @AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<TaskResponse>> {
        val tasks = taskService.getTasksByRoom(roomId, principal.getId())
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/completed")
    fun getCompletedTasksByRoom(@PathVariable roomId: String, @AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<TaskResponse>> {
        val tasks = taskService.getTasksByRoomAndStatus(roomId, principal.getId(), isComplete = true)
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/status")
    fun getTasksByRoomAndStatus(
        @PathVariable roomId: String,
        @RequestParam isComplete: Boolean,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<TaskResponse>> {
        val tasks = taskService.getTasksByRoomAndStatus(roomId, principal.getId(), isComplete)
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/upcoming")
    fun getUpcomingTasks(@PathVariable roomId: String, @AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<TaskResponse>> {
        val tasks = taskService.getUpComingTasks(roomId, principal.getId())
        return ResponseEntity.ok(tasks)
    }

    @PatchMapping("/{taskId}/complete")
    @Suppress("UNUSED_PARAMETER")
    fun markTaskComplete(
        @PathVariable roomId: String,
        @PathVariable taskId: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<TaskResponse> {
        val updated = taskService.markTaskComplete(roomId, taskId, principal.getId())
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{taskId}")
    @Suppress("UNUSED_PARAMETER")
    fun deleteTask(
        @PathVariable roomId: String,
        @PathVariable taskId: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<String> {
        taskService.deleteTask(roomId, taskId, principal.getId())
        return ResponseEntity.ok("Task deleted successfully")
    }

    @PostMapping("/recurring/regenerate")
    @Suppress("UNUSED_PARAMETER")
    fun regenerateRecurringTasks(@PathVariable roomId: String, @AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<String> {
        taskService.regenerateRecurringTasks(roomId, principal.getId())
        return ResponseEntity.ok("Recurring tasks regenerated")
    }



    @GetMapping("/user/{userId}")
    fun getTasksByUser(@PathVariable userId: String, @AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<TaskResponse>> {
        val tasks = taskService.getTasksByUser(userId, principal.getId())
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/user/{userId}/status")
    fun getTasksByUserAndStatus(
        @PathVariable userId: String,
        @RequestParam isComplete: Boolean,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<TaskResponse>> {
        val tasks = taskService.getTasksByUserAndStatus(userId, principal.getId(), isComplete)
        return ResponseEntity.ok(tasks)
    }
}
