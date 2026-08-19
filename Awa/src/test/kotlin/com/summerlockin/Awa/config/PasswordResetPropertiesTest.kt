package com.summerlockin.Awa.config

import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class PasswordResetPropertiesTest {
    @Test
    fun `production frontend reset URL must use https`() {
        val properties = PasswordResetProperties(frontendUrl = "http://app.example.com/reset-password")

        assertFailsWith<IllegalArgumentException> {
            properties.validate()
        }
    }

    @Test
    fun `localhost frontend reset URL may use http`() {
        PasswordResetProperties(frontendUrl = "http://localhost:3000/reset-password").validate()
    }
}
