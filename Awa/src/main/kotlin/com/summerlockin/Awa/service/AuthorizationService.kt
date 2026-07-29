package com.summerlockin.Awa.service

import com.summerlockin.Awa.exception.NotFoundException
import com.summerlockin.Awa.model.Task
import com.summerlockin.Awa.repository.RoomRepository
import com.summerlockin.Awa.repository.userRepository
import org.bson.types.ObjectId
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val userRepository: userRepository,
    private val roomRepository: RoomRepository,
) {

    fun requireSelf(actingUserId: String, targetUserId: String, message: String = "Forbidden") {
        if (actingUserId != targetUserId) {
            throw AccessDeniedException(message)
        }
    }

    fun requireRoomMember(roomId: String, userId: String) {
        val user = findUser(userId)
        val resolvedRoomId = ObjectId(roomId)
        if (user.roomId != resolvedRoomId) {
            throw AccessDeniedException("You are not a member of this room")
        }
    }

    fun requireRoomOwner(roomId: String, userId: String) {
        val room = roomRepository.findById(ObjectId(roomId)).orElseThrow { NotFoundException("Room not found") }
        if (room.ownerId != ObjectId(userId)) {
            throw AccessDeniedException("Only the room owner can perform this action")
        }
    }

    fun requireTaskRoomAccess(task: Task, userId: String) {
        requireRoomMember(task.roomId.toHexString(), userId)
    }

    fun requireTaskOwnerOrAssignee(task: Task, userId: String) {
        val currentUserId = ObjectId(userId)
        if (task.assignedTo != null && task.assignedTo == currentUserId) {
            return
        }

        val room = roomRepository.findById(task.roomId).orElseThrow { NotFoundException("Room not found") }
        if (room.ownerId != currentUserId) {
            throw AccessDeniedException("You are not allowed to modify this task")
        }
    }

    fun requireMessageRoomAccess(roomId: String, userId: String) {
        requireRoomMember(roomId, userId)
    }

    fun requireBillRoomAccess(roomId: String, userId: String) {
        requireRoomMember(roomId, userId)
    }

    fun requireShoppingItemRoomAccess(roomId: String, userId: String) {
        requireRoomMember(roomId, userId)
    }

    fun requireUsersInRoom(roomId: String, userIds: Collection<String>) {
        val roomObjectId = ObjectId(roomId)
        val allowed = userRepository.findByRoomId(roomObjectId).mapNotNull { it.id?.toHexString() }.toSet()
        val invalid = userIds.filterNot { allowed.contains(it) }
        if (invalid.isNotEmpty()) {
            throw AccessDeniedException("All selected users must be members of the room")
        }
    }

    private fun findUser(userId: String) =
        userRepository.findById(ObjectId(userId)).orElseThrow { NotFoundException("User not found") }
}