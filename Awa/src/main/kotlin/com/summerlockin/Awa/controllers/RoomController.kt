package com.summerlockin.Awa.controllers

import com.summerlockin.Awa.DTO.RoomCreateRequest
import com.summerlockin.Awa.DTO.RoomResponse
import com.summerlockin.Awa.DTO.RoomUpdateRequest
import com.summerlockin.Awa.service.RoomService
import org.apache.coyote.Response
import org.springframework.http.ResponseEntity
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
    private val roomService: RoomService
) {

    @PostMapping
    fun createRoom(@RequestBody request : RoomCreateRequest) : ResponseEntity<RoomResponse>{
        val createdRoom  = roomService.createRoom(request)
        return ResponseEntity.status(201).body(createdRoom)
    }


    @GetMapping("/{code}")
    fun getRoomByCode(@PathVariable code :String):ResponseEntity<RoomResponse>{
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