package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.RoomCreateRequest
import com.summerlockin.Awa.DTO.RoomResponse
import com.summerlockin.Awa.DTO.RoomUpdateRequest
import com.summerlockin.Awa.exception.NotFoundException
import com.summerlockin.Awa.model.Room
import com.summerlockin.Awa.repository.RoomRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RoomService(
    private val roomRepository: RoomRepository
) {
    fun createRoom(request:RoomCreateRequest):RoomResponse {
        val joinCode = generateJoinCode()
        val room = Room(
            name = request.name,
            code =  joinCode,
            ownerId = ObjectId(request.ownerID),
            createdAt = Instant.now()
        )
         return roomRepository.save(room).toDTO()
    }

    fun getRoomByCode(code: String): RoomResponse {
        val room = roomRepository.findByCode(code)
            ?: throw IllegalArgumentException("Room with code $code not found")
        return room.toDTO()
    }

    fun updateRoom(roomId: String, request: RoomUpdateRequest): RoomResponse {
        val room = roomRepository.findById(ObjectId(roomId))
            .orElseThrow { NotFoundException("Room not found with ID $roomId") }

        val updatedRoom = room.copy(
            name = request.name ?: room.name,
            code = request.code ?: room.code
        )

        return roomRepository.save(updatedRoom).toDTO()
    }










    private fun Room.toDTO(): RoomResponse {
        return RoomResponse(
            id = this.id.toString(),
            name = this.name,
            code = this.code,
            ownerId = this.ownerId.toString(),
            createdAt = this.createdAt.toString()
        )
    }

    private fun generateJoinCode(): String {
        var code: String
        do {
            code = List(6) { (('A'..'Z') + ('0'..'9')).random() }.joinToString("")
        } while (roomRepository.findByCode(code) != null)
        return code
    }

}