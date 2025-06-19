package com.summerlockin.Awa.service

import com.summerlockin.Awa.DTO.UserRegisterRequest
import com.summerlockin.Awa.DTO.UserResponse
import com.summerlockin.Awa.DTO.UserUpdateRequest
import com.summerlockin.Awa.exception.AlreadyExistsException
import com.summerlockin.Awa.exception.NotFoundException
import com.summerlockin.Awa.model.User
import com.summerlockin.Awa.repository.userRepository
import com.summerlockin.Awa.security.PasswordEncoder
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: userRepository,
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

    private fun User.toDTO(): UserResponse {
        return UserResponse(
            id = this.id.toString(),
            firstName = this.firstname,
            email = this.email,
            createdAt = this.joinedAt.toString()
        )
    }

}