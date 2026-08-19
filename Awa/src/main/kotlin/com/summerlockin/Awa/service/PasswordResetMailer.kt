package com.summerlockin.Awa.service

import com.summerlockin.Awa.config.MailProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpHeaders
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

interface PasswordResetMailer {
    fun sendPasswordReset(
        email: String,
        firstName: String,
        resetLink: String,
        expiresInMinutes: Long,
        idempotencyKey: String
    )
}

@Service
@ConditionalOnProperty(prefix = "app.mail", name = ["provider"], havingValue = "resend")
class ResendPasswordResetMailer(
    restClientBuilder: RestClient.Builder,
    private val properties: MailProperties,
    private val template: PasswordResetEmailTemplate
) : PasswordResetMailer {
    private val logger = LoggerFactory.getLogger(ResendPasswordResetMailer::class.java)
    private val restClient: RestClient = run {
        require(properties.resendApiKey.isNotBlank()) {
            "RESEND_API_KEY is required when MAIL_PROVIDER=resend"
        }
        require(properties.from.isNotBlank()) {
            "MAIL_FROM is required when MAIL_PROVIDER=resend"
        }
        restClientBuilder
            .baseUrl("https://api.resend.com")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${properties.resendApiKey.trim()}")
            .defaultHeader(HttpHeaders.USER_AGENT, "Awa/1.0")
            .build()
    }

    @Async("passwordResetMailExecutor")
    override fun sendPasswordReset(
        email: String,
        firstName: String,
        resetLink: String,
        expiresInMinutes: Long,
        idempotencyKey: String
    ) {
        val rendered = template.render(firstName, resetLink, expiresInMinutes)
        val payload = linkedMapOf<String, Any>(
            "from" to properties.from.trim(),
            "to" to listOf(email),
            "subject" to rendered.subject,
            "html" to rendered.html,
            "text" to rendered.text
        )
        properties.replyTo?.trim()?.takeIf { it.isNotEmpty() }?.let {
            payload["reply_to"] = it
        }

        try {
            restClient.post()
                .uri("/emails")
                .header("Idempotency-Key", idempotencyKey)
                .body(payload)
                .retrieve()
                .toBodilessEntity()
        } catch (ex: Exception) {
            logger.error("Password-reset email delivery failed through Resend", ex)
        }
    }
}

@Service
@ConditionalOnProperty(prefix = "app.mail", name = ["provider"], havingValue = "log", matchIfMissing = true)
class DevelopmentPasswordResetMailer : PasswordResetMailer {
    private val logger = LoggerFactory.getLogger(DevelopmentPasswordResetMailer::class.java)

    @Async("passwordResetMailExecutor")
    override fun sendPasswordReset(
        email: String,
        firstName: String,
        resetLink: String,
        expiresInMinutes: Long,
        idempotencyKey: String
    ) {
        logger.warn(
            "Development password-reset email for {} (expires in {} minutes): {}",
            email,
            expiresInMinutes,
            resetLink
        )
    }
}
