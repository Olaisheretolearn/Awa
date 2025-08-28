package com.summerlockin.Awa.controllers

import com.summerlockin.Awa.DTO.*
import com.summerlockin.Awa.exception.NotFoundException
import com.summerlockin.Awa.repository.userRepository
import com.summerlockin.Awa.security.UserPrincipal
import com.summerlockin.Awa.service.RoomService
import com.summerlockin.Awa.service.UserService
import org.apache.coyote.Response
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/rooms")
class RoomController(
    private val roomService: RoomService,
    private val userService: UserService,
    private val userRepository: userRepository
) {

    @PostMapping
    fun createRoom(@RequestBody request : RoomCreateRequest) :ResponseEntity<RoomResponse>{
        val createdRoom  = roomService.createRoom(request)
        return ResponseEntity.status(201).body(createdRoom)
    }


    @GetMapping("/{roomId}/members")
    fun getMembers(@PathVariable roomId: String): ResponseEntity<List<UserResponse>> {
        roomService.ensureExists(roomId)
        return ResponseEntity.ok(userService.getUsersInRoom(roomId))
    }



    @GetMapping("/me")
    fun getMyRoom(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<MyRoomResponse> {
        val userId = principal.getId()
        val user = userRepository.findById(org.bson.types.ObjectId(userId))
            .orElseThrow { NotFoundException("User not found") }

        val roomId = user.roomId?.toHexString()
        if (roomId == null) {
            return ResponseEntity.ok(MyRoomResponse(null, emptyList()))
        }

        val room = roomService.getRoom(roomId)
        val members = userService.getUsersInRoom(roomId)
        return ResponseEntity.ok(MyRoomResponse(room, members))
    }

    @GetMapping("/id/{roomId}")
    fun getRoomById(@PathVariable roomId: String): ResponseEntity<RoomResponse> {
        val room = roomService.getRoom(roomId)  // service already has this
        return ResponseEntity.ok(room)
    }

    @GetMapping("/code/{code}")
    fun getRoomByCode(@PathVariable code: String): ResponseEntity<RoomResponse> {
        val room = roomService.getRoomByCode(code)
        return ResponseEntity.ok(room)
    }




    @PatchMapping("/{roomId}")
    fun updateRoom(@PathVariable roomId: String, @RequestBody request: RoomUpdateRequest
    ): ResponseEntity<RoomResponse> {
        val updatedRoom = roomService.updateRoom(roomId, request)
        return ResponseEntity.ok(updatedRoom)
    }



}