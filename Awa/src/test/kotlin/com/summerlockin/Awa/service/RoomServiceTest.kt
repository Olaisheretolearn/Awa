package com.summerlockin.Awa.service

import com.summerlockin.Awa.exception.NotFoundException
import com.summerlockin.Awa.model.Room
import com.summerlockin.Awa.repository.RoomRepository
import com.summerlockin.Awa.repository.userRepository
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.dao.DuplicateKeyException
import org.springframework.security.access.AccessDeniedException
import java.time.Instant
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RoomServiceTest {
    private val roomRepository = mock<RoomRepository>()
    private val userRepository = mock<userRepository>()
    private val authorizationService = mock<AuthorizationService>()
    private val roomService = RoomService(roomRepository, userRepository, authorizationService)

    @Test
    fun `rotate share code replaces the old code and returns the complete room`() {
        val roomId = ObjectId()
        val ownerId = ObjectId()
        val createdAt = Instant.parse("2026-08-12T12:00:00Z")
        val room = Room(
            id = roomId,
            name = "Summer House",
            code = "ABC123",
            ownerId = ownerId,
            createdAt = createdAt,
            city = "Winnipeg"
        )
        whenever(roomRepository.findById(roomId)).thenReturn(Optional.of(room))
        whenever(roomRepository.findByCode(any())).thenReturn(null)
        whenever(roomRepository.save(any<Room>())).thenAnswer { it.getArgument(0) }

        val response = roomService.rotateShareCode(roomId.toHexString(), ownerId.toHexString())

        val savedRoom = argumentCaptor<Room>()
        verify(roomRepository).save(savedRoom.capture())
        verify(authorizationService).requireRoomOwner(roomId.toHexString(), ownerId.toHexString())
        assertNotEquals("ABC123", savedRoom.firstValue.code)
        assertTrue(savedRoom.firstValue.code.matches(Regex("[A-Z0-9]{6}")))
        assertEquals(savedRoom.firstValue.code, response.code)
        assertEquals(roomId.toHexString(), response.id)
        assertEquals("Summer House", response.name)
        assertEquals(ownerId.toHexString(), response.ownerId)
        assertEquals("Winnipeg", response.city)
        assertEquals(createdAt.toString(), response.createdAt)
    }

    @Test
    fun `rotate share code retries a database uniqueness collision`() {
        val roomId = ObjectId()
        val ownerId = ObjectId()
        val room = Room(id = roomId, name = "Home", code = "OLD123", ownerId = ownerId)
        whenever(roomRepository.findById(roomId)).thenReturn(Optional.of(room))
        whenever(roomRepository.findByCode(any())).thenReturn(null)
        whenever(roomRepository.save(any<Room>()))
            .thenThrow(DuplicateKeyException("code collision"))
            .thenAnswer { it.getArgument(0) }

        val response = roomService.rotateShareCode(roomId.toHexString(), ownerId.toHexString())

        verify(roomRepository, times(2)).save(any<Room>())
        assertNotEquals(room.code, response.code)
    }

    @Test
    fun `rotate share code does not change the room when every persistence attempt fails`() {
        val roomId = ObjectId()
        val ownerId = ObjectId()
        val room = Room(id = roomId, name = "Home", code = "KEEP12", ownerId = ownerId)
        whenever(roomRepository.findById(roomId)).thenReturn(Optional.of(room))
        whenever(roomRepository.findByCode(any())).thenReturn(null)
        whenever(roomRepository.save(any<Room>())).thenThrow(DuplicateKeyException("code collision"))

        assertFailsWith<IllegalStateException> {
            roomService.rotateShareCode(roomId.toHexString(), ownerId.toHexString())
        }

        verify(roomRepository, times(5)).save(any<Room>())
        assertEquals("KEEP12", room.code)
    }

    @Test
    fun `rotate share code is restricted to the room owner`() {
        val roomId = ObjectId().toHexString()
        val memberId = ObjectId().toHexString()
        doThrow(AccessDeniedException("Only the room owner can perform this action"))
            .whenever(authorizationService).requireRoomOwner(roomId, memberId)

        assertFailsWith<AccessDeniedException> {
            roomService.rotateShareCode(roomId, memberId)
        }

        verifyNoInteractions(roomRepository)
    }

    @Test
    fun `rotate share code reports a missing room without writing`() {
        val roomId = ObjectId()
        val ownerId = ObjectId()
        whenever(roomRepository.findById(roomId)).thenReturn(Optional.empty())

        assertFailsWith<NotFoundException> {
            roomService.rotateShareCode(roomId.toHexString(), ownerId.toHexString())
        }

        verify(roomRepository, never()).save(any<Room>())
    }
}
