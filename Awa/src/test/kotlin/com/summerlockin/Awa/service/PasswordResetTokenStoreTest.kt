package com.summerlockin.Awa.service

import com.summerlockin.Awa.model.PasswordResetToken
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame

class PasswordResetTokenStoreTest {
    private val mongoTemplate = mock<MongoTemplate>()
    private val store = PasswordResetTokenStore(mongoTemplate)

    @Test
    fun `consume valid performs one atomic conditional update`() {
        val now = Instant.parse("2026-08-14T12:00:00Z")
        val consumed = PasswordResetToken(
            tokenHash = "stored-hash",
            userId = ObjectId(),
            expiresAt = now.plusSeconds(60),
            used = true,
            usedAt = now
        )
        whenever(
            mongoTemplate.findAndModify(
                any<Query>(),
                any<Update>(),
                any<FindAndModifyOptions>(),
                eq(PasswordResetToken::class.java)
            )
        ).thenReturn(consumed)

        val result = store.consumeValid("stored-hash", now)

        val query = argumentCaptor<Query>()
        val update = argumentCaptor<Update>()
        verify(mongoTemplate).findAndModify(
            query.capture(),
            update.capture(),
            any<FindAndModifyOptions>(),
            eq(PasswordResetToken::class.java)
        )
        assertSame(consumed, result)
        assertEquals("stored-hash", query.firstValue.queryObject.getString("token"))
        assertFalse(query.firstValue.queryObject.getBoolean("used"))
        assertEquals(now, update.firstValue.updateObject.get("${'$'}set", org.bson.Document::class.java)["usedAt"])
    }
}
