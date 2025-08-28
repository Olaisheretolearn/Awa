package com.summerlockin.Awa.controllers

import com.summerlockin.Awa.DTO.JoinRoomRequest
import com.summerlockin.Awa.DTO.UserRegisterRequest
import com.summerlockin.Awa.DTO.UserResponse
import com.summerlockin.Awa.DTO.UserUpdateRequest
import com.summerlockin.Awa.security.JwtService
import com.summerlockin.Awa.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController (
    private val userService: UserService,
    private val jwtService: JwtService
) {
    @GetMapping("/me")
    fun me(@RequestHeader("Authorization") bearer: String): UserResponse {
        val token = bearer.removePrefix("Bearer ").trim()
        val userId = jwtService.getUserIdFromToken(token)
        return userService.findUserById(userId)
    }



    @PatchMapping("/{userId}/avatar")
    fun setAvatar(
        @PathVariable userId: String,
        @RequestBody body: Map<String, String>
    ): UserResponse {
        val avatarId = body["avatarId"] ?: throw IllegalArgumentException("avatarId is required")
        return userService.updateUser(userId, UserUpdateRequest(avatarId = avatarId))
    }








    @PostMapping("/register")
    fun createUser(@RequestBody request : UserRegisterRequest): ResponseEntity<UserResponse> {
        val createdUser = userService.createUser(request)
        return ResponseEntity.ok(createdUser)
    }

    @GetMapping("/room/{roomId}")
    fun getUsersByRoom(@PathVariable roomId: String): ResponseEntity<List<UserResponse>> {
        val users = userService.getUsersInRoom(roomId)
        return ResponseEntity.ok(users)
    }


    @PostMapping("/{userId}/join-room")
    fun joinRoom(@PathVariable userId: String, @RequestBody request: JoinRoomRequest): ResponseEntity<UserResponse> {
        val updatedUser = userService.joinRoom(userId, request.code)
        return ResponseEntity.ok(updatedUser)
    }

    @PatchMapping("/{userId}/leave-room")
    fun leaveRoom(@PathVariable userId: String): ResponseEntity<UserResponse> {
        val updatedUser = userService.leaveRoom(userId)
        return ResponseEntity.ok(updatedUser)
    }



    @GetMapping("/{id}")
    fun findUser(@PathVariable id :String ):ResponseEntity<UserResponse>{
        val user = userService.findUserById(id)
        return ResponseEntity.ok(user)
    }

    @PatchMapping("/{id}")
    fun updateUser(
        @PathVariable id:String,
        @RequestBody request : UserUpdateRequest
    ):ResponseEntity<UserResponse>{
        val updatedUser = userService.updateUser(id,request)
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/{id}")
    fun deactivateUser(@PathVariable id: String): ResponseEntity<UserResponse> {
        val deactivatedUser = userService.deactivateUser(id)
        return ResponseEntity.ok(deactivatedUser)
    }




}