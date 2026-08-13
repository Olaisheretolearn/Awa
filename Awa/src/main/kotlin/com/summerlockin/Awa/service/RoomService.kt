package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.RoomCreateRequest
import com.summerlockin.Awa.DTO.RoomResponse
import com.summerlockin.Awa.DTO.RoomUpdateRequest
import com.summerlockin.Awa.exception.NotFoundException
import com.summerlockin.Awa.model.Role
import com.summerlockin.Awa.model.Room
import com.summerlockin.Awa.repository.RoomRepository
import com.summerlockin.Awa.repository.userRepository
import org.bson.types.ObjectId
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Instant

@Service
class RoomService(
    private val roomRepository: RoomRepository,
    private val userRepository: userRepository,
    private val authorizationService: AuthorizationService
) {
    private val secureRandom = SecureRandom()

    fun createRoom(ownerId: String, request:RoomCreateRequest):RoomResponse {
        val joinCode = generateJoinCode()
        val room = Room(
            name = request.name,
            code =  joinCode,
            ownerId = ObjectId(ownerId),
            city = request.city?.trim().takeUnless { it.isNullOrBlank() },
            createdAt = Instant.now(),

        )
        val saved = roomRepository.save(room)

        val owner = userRepository.findById(ObjectId(ownerId))
            .orElseThrow { NotFoundException("Owner user not found") }

        if (owner.roomId == null) {
            val updatedOwner = owner.copy(roomId = saved.id, role = Role.OWNER)
            userRepository.save(updatedOwner)
        }

        return saved.toDTO()
    }




    fun getRoomByCode(code: String, actingUserId: String): RoomResponse {
        val room = roomRepository.findByCode(code)
            ?: throw IllegalArgumentException("Room with code $code not found")
        authorizationService.requireRoomMember(room.id?.toHexString() ?: throw NotFoundException("Room not found"), actingUserId)
        return room.toDTO()
    }

    fun updateRoom(roomId: String, actingUserId: String, request: RoomUpdateRequest): RoomResponse {
        authorizationService.requireRoomOwner(roomId, actingUserId)
        val room = roomRepository.findById(ObjectId(roomId))
            .orElseThrow { NotFoundException("Room not found with ID $roomId") }

        val updatedRoom = room.copy(
            name = request.name ?: room.name,
            city  = request.city?.trim().takeUnless { it.isNullOrBlank() } ?: room.city
        )

        return roomRepository.save(updatedRoom).toDTO()
    }

    fun rotateShareCode(roomId: String, actingUserId: String): RoomResponse {
        authorizationService.requireRoomOwner(roomId, actingUserId)
        val room = roomRepository.findById(ObjectId(roomId))
            .orElseThrow { NotFoundException("Room not found with ID $roomId") }

        repeat(CODE_PERSIST_ATTEMPTS) { attempt ->
            val newCode = generateJoinCode(excluding = room.code)

            try {
                // MongoDB replaces one document atomically: the old code stops resolving
                // in the same write that makes the new code available.
                return roomRepository.save(room.copy(code = newCode)).toDTO()
            } catch (ex: DuplicateKeyException) {
                if (attempt == CODE_PERSIST_ATTEMPTS - 1) {
                    throw IllegalStateException("Could not generate a unique room share code", ex)
                }
            }
        }

        error("Could not generate a unique room share code")
    }



    fun ensureExists(roomId: String) {
        if (!roomRepository.existsById(ObjectId(roomId))) {
            throw NotFoundException("ROOM_CODE_INVALID")
        }
    }

    fun getRoom(roomId: String, actingUserId: String): RoomResponse {
        authorizationService.requireRoomMember(roomId, actingUserId)
        return roomRepository.findById(ObjectId(roomId))
            .orElseThrow { NotFoundException("Room not found") }
            .toDTO()
    }







    private fun Room.toDTO(): RoomResponse {
        return RoomResponse(
            id = this.id.toString(),
            name = this.name,
            code = this.code,
            ownerId = this.ownerId.toString(),
            createdAt = this.createdAt.toString(),
            city = city,
        )
    }

    private fun generateJoinCode(excluding: String? = null): String {
        var code: String
        do {
            code = buildString(CODE_LENGTH) {
                repeat(CODE_LENGTH) {
                    append(CODE_ALPHABET[secureRandom.nextInt(CODE_ALPHABET.length)])
                }
            }
        } while (code == excluding || roomRepository.findByCode(code) != null)
        return code
    }

    private companion object {
        const val CODE_LENGTH = 6
        const val CODE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        const val CODE_PERSIST_ATTEMPTS = 5
    }

}
