package com.summerlockin.Awa.service

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordResetEmailTemplateTest {
    private val template = PasswordResetEmailTemplate()

    @Test
    fun `template includes expiry and both html and text links`() {
        val link = "https://app.example/reset?token=abc123"

        val email = template.render("Sam", link, 30)

        assertTrue(email.subject.contains("Reset"))
        assertTrue(email.html.contains("30 minutes"))
        assertTrue(email.html.contains(link.replace("&", "&amp;")))
        assertTrue(email.text.contains(link))
    }

    @Test
    fun `template escapes untrusted display names`() {
        val email = template.render("<script>alert(1)</script>", "https://app.example/reset", 30)

        assertFalse(email.html.contains("<script>"))
        assertTrue(email.html.contains("&lt;script&gt;"))
    }
}
