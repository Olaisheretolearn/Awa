package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.UserRegisterRequest
import com.summerlockin.Awa.DTO.UserResponse
import com.summerlockin.Awa.DTO.UserUpdateRequest
import com.summerlockin.Awa.exception.AlreadyExistsException
import com.summerlockin.Awa.exception.NotFoundException
import com.summerlockin.Awa.model.User
import com.summerlockin.Awa.repository.RoomRepository
import com.summerlockin.Awa.repository.userRepository
import com.summerlockin.Awa.security.PasswordEncoder
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: userRepository,
    private val roomRepository: RoomRepository,
    //inject encoder later
    private val encoder : PasswordEncoder
) {

    fun createUser(request :UserRegisterRequest):UserResponse{
        if(userRepository.findByEmailIgnoreCase(request.email) != null){
            throw IllegalArgumentException("User with email already exists")
        }

        val user = User(
            email = request.email.trim().lowercase(),
            password = encoder.encode(request.password),
            firstname = request.firstName
        )
        return userRepository.save(user).toDTO()
    }

    fun findUserById(userId :String ):UserResponse{
        val user = userRepository.findById(ObjectId(userId))
            .orElseThrow{
                NotFoundException("User not found")
            }
        return user.toDTO()
    }

    fun deactivateUser(userId:String):UserResponse {
        val user = userRepository.findById(ObjectId(userId))
            .orElseThrow{
                AlreadyExistsException("User with email already exists")
            }
        val updated = user.copy(isActive = false)
        return userRepository.save(updated).toDTO()
            }



    fun updateUser(userId: String, update:UserUpdateRequest):UserResponse{
        val user = userRepository.findById(ObjectId(userId))
            .orElseThrow {
                RuntimeException("User not found")
            }
        val updated = user.copy(
            email = update.email ?: user.email,
            firstname = update.firstname ?: user.firstname
        )

        return userRepository.save(updated).toDTO()
    }

    fun joinRoom(userId: String, joinCode: String): UserResponse {
        val room = roomRepository.findByCode(joinCode)
            ?: throw NotFoundException("Room with code $joinCode not found")

        val user = userRepository.findById(ObjectId(userId))
            .orElseThrow { NotFoundException("User not found") }

        //already in a room?
        if (user.roomId != null) {
            throw AlreadyExistsException("User is already in a room")
        }

        val updated = user.copy(roomId = room.id)
        return userRepository.save(updated).toDTO()
    }

    fun leaveRoom(userId: String): UserResponse {
        val user = userRepository.findById(ObjectId(userId))
            .orElseThrow { NotFoundException("User not found") }

        if (user.roomId == null) {
            throw IllegalStateException("User is not in a room")
        }

        val updated = user.copy(roomId = null)
        return userRepository.save(updated).toDTO()
    }

    fun getUsersInRoom(roomId: String): List<UserResponse> {
        val objectId = ObjectId(roomId)
        val users = userRepository.findByRoomId(objectId)
        return users.map { it.toDTO() }
    }






    private fun User.toDTO(): UserResponse {
        return UserResponse(
            id = this.id.toString(),
            firstName = this.firstname,
            email = this.email,
            createdAt = this.joinedAt.toString(),
            roomId = this.roomId?.toString(),
            role = this.role.name
        )
    }



}