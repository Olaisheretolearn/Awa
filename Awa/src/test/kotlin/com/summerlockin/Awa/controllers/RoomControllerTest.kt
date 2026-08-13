package com.summerlockin.Awa.controllers

import com.summerlockin.Awa.DTO.RoomResponse
import com.summerlockin.Awa.model.User
import com.summerlockin.Awa.repository.userRepository
import com.summerlockin.Awa.security.UserPrincipal
import com.summerlockin.Awa.service.RoomService
import com.summerlockin.Awa.service.UserService
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class RoomControllerTest {
    private val roomService = mock<RoomService>()
    private val userService = mock<UserService>()
    private val userRepository = mock<userRepository>()
    private val controller = RoomController(roomService, userService, userRepository)

    @Test
    fun `rotate endpoint returns the complete updated room`() {
        val roomId = ObjectId().toHexString()
        val ownerId = ObjectId()
        val principal = UserPrincipal(
            User(id = ownerId, firstname = "Owner", email = "owner@example.com", password = "hash")
        )
        val expected = RoomResponse(
            id = roomId,
            name = "Home",
            code = "XYZ789",
            ownerId = ownerId.toHexString(),
            city = "Winnipeg",
            createdAt = "2026-08-12T12:00:00Z"
        )
        whenever(roomService.rotateShareCode(roomId, ownerId.toHexString())).thenReturn(expected)

        val result = controller.rotateShareCode(roomId, principal)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(expected, result.body)
        verify(roomService).rotateShareCode(roomId, ownerId.toHexString())
    }

    @Test
    fun `rotate endpoint is mapped to the frontend contract`() {
        val method = RoomController::class.java.getDeclaredMethod(
            "rotateShareCode",
            String::class.java,
            UserPrincipal::class.java
        )

        assertContentEquals(
            arrayOf("/{roomId}/share-code/rotate"),
            method.getAnnotation(PostMapping::class.java).value
        )
    }
}
