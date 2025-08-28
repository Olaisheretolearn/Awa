package com.summerlockin.Awa.controllers

import com.summerlockin.Awa.DTO.TaskCreateRequest
import com.summerlockin.Awa.DTO.TaskResponse
import com.summerlockin.Awa.DTO.TaskUpdateRequest
import com.summerlockin.Awa.service.TaskService
import org.bson.types.ObjectId
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

@RestController
@RequestMapping("/api/room/{roomId}/task")
class TaskController(
    private val taskService: TaskService
) {

    @PostMapping
    fun createTask(
        @PathVariable roomId: String,
        @RequestBody req: TaskCreateRequest
    ): ResponseEntity<TaskResponse> =
        ResponseEntity.status(201).body(taskService.createTask(roomId, req))




    @PatchMapping("/{taskId}")
    fun updateTask(
        @PathVariable roomId: String,
        @PathVariable taskId: String,
        @RequestBody request: TaskUpdateRequest
    ): ResponseEntity<TaskResponse> {
        val updatedTask = taskService.updateTask(taskId, request)
        return ResponseEntity.ok(updatedTask)
    }

    @GetMapping("/{taskId}")
    fun getTask(
        @PathVariable roomId: String,
        @PathVariable taskId: String
    ): ResponseEntity<TaskResponse> {
        val task = taskService.getTaskById(taskId)
        return ResponseEntity.ok(task)
    }

    @GetMapping
    fun getTasksByRoom(@PathVariable roomId: String): ResponseEntity<List<TaskResponse>> {
        val tasks = taskService.getTasksByRoom(roomId)
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/completed")
    fun getCompletedTasksByRoom(@PathVariable roomId: String): ResponseEntity<List<TaskResponse>> {
        val tasks = taskService.getTasksByRoomAndStatus(roomId, isComplete = true)
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/status")
    fun getTasksByRoomAndStatus(
        @PathVariable roomId: String,
        @RequestParam isComplete: Boolean
    ): ResponseEntity<List<TaskResponse>> {
        val tasks = taskService.getTasksByRoomAndStatus(roomId, isComplete)
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/upcoming")
    fun getUpcomingTasks(@PathVariable roomId: String): ResponseEntity<List<TaskResponse>> {
        val tasks = taskService.getUpComingTasks(roomId)
        return ResponseEntity.ok(tasks)
    }

    @PatchMapping("/{taskId}/complete")
    fun markTaskComplete(
        @PathVariable roomId: String,
        @PathVariable taskId: String,
        @RequestParam userId: String
    ): ResponseEntity<TaskResponse> {
        val updated = taskService.markTaskComplete(taskId, userId)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{taskId}")
    fun deleteTask(
        @PathVariable roomId: String,
        @PathVariable taskId: String
    ): ResponseEntity<String> {
        taskService.deleteTask(taskId)
        return ResponseEntity.ok("Task deleted successfully")
    }

    @PostMapping("/recurring/regenerate")
    fun regenerateRecurringTasks(@PathVariable roomId: String): ResponseEntity<String> {
        taskService.regenerateRecurringTasks()
        return ResponseEntity.ok("Recurring tasks regenerated")
    }



    @GetMapping("/user/{userId}")
    fun getTasksByUser(@PathVariable userId: String): ResponseEntity<List<TaskResponse>> {
        val tasks = taskService.getTasksByUser(userId)
        return ResponseEntity.ok(tasks)
    }

    @GetMapping("/user/{userId}/status")
    fun getTasksByUserAndStatus(
        @PathVariable userId: String,
        @RequestParam isComplete: Boolean
    ): ResponseEntity<List<TaskResponse>> {
        val tasks = taskService.getTasksByUserAndStatus(userId, isComplete)
        return ResponseEntity.ok(tasks)
    }
}
