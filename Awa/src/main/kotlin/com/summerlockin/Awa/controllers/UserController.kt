package com.summerlockin.Awa.controllers

import com.summerlockin.Awa.DTO.JoinRoomRequest
import com.summerlockin.Awa.DTO.UserRegisterRequest
import com.summerlockin.Awa.DTO.UserResponse
import com.summerlockin.Awa.DTO.UserUpdateRequest
import com.summerlockin.Awa.security.UserPrincipal
import com.summerlockin.Awa.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController (
    private val userService: UserService,
) {
    @GetMapping("/me")
    fun me(@AuthenticationPrincipal principal: UserPrincipal): UserResponse {
        return userService.findUserById(principal.getId(), principal.getId())
    }



    @PatchMapping("/{userId}/avatar")
    fun setAvatar(
        @PathVariable userId: String,
        @RequestBody body: Map<String, String>,
        @AuthenticationPrincipal principal: UserPrincipal
    ): UserResponse {
        val avatarId = body["avatarId"] ?: throw IllegalArgumentException("avatarId is required")
        return userService.updateUser(userId, principal.getId(), UserUpdateRequest(avatarId = avatarId))
    }








    @PostMapping("/register")
    fun createUser(@Valid @RequestBody request : UserRegisterRequest): ResponseEntity<UserResponse> {
        val createdUser = userService.createUser(request)
        return ResponseEntity.ok(createdUser)
    }

    @GetMapping("/room/{roomId}")
    fun getUsersByRoom(@PathVariable roomId: String, @AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<UserResponse>> {
        val users = userService.getUsersInRoom(roomId, principal.getId())
        return ResponseEntity.ok(users)
    }


    @PostMapping("/{userId}/join-room")
    fun joinRoom(@PathVariable userId: String, @RequestBody request: JoinRoomRequest, @AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<UserResponse> {
        val updatedUser = userService.joinRoom(userId, principal.getId(), request.code)
        return ResponseEntity.ok(updatedUser)
    }

    @PatchMapping("/{userId}/leave-room")
    fun leaveRoom(@PathVariable userId: String, @AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<UserResponse> {
        val updatedUser = userService.leaveRoom(userId, principal.getId())
        return ResponseEntity.ok(updatedUser)
    }



    @GetMapping("/{id}")
    fun findUser(@PathVariable id :String, @AuthenticationPrincipal principal: UserPrincipal):ResponseEntity<UserResponse>{
        val user = userService.findUserById(id, principal.getId())
        return ResponseEntity.ok(user)
    }

    @PatchMapping("/{id}")
    fun updateUser(
        @PathVariable id:String,
        @RequestBody request : UserUpdateRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ):ResponseEntity<UserResponse>{
        val updatedUser = userService.updateUser(id, principal.getId(), request)
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/{id}")
    fun deactivateUser(@PathVariable id: String, @AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<UserResponse> {
        val deactivatedUser = userService.deactivateUser(id, principal.getId())
        return ResponseEntity.ok(deactivatedUser)
    }




}
