package com.summerlockin.Awa.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI
import java.time.Duration

@ConfigurationProperties("app.password-reset")
data class PasswordResetProperties(
    var frontendUrl: String = "http://localhost:3000/reset-password",
    var tokenTtl: Duration = Duration.ofMinutes(30),
    var requestCooldown: Duration = Duration.ofSeconds(60)
) {
    fun validate() {
        val uri = runCatching { URI(frontendUrl) }
            .getOrElse { throw IllegalStateException("app.password-reset.frontend-url must be a valid URL", it) }

        val localHttp = uri.scheme == "http" && uri.host in setOf("localhost", "127.0.0.1", "[::1]")
        require(uri.host != null && (uri.scheme == "https" || localHttp)) {
            "app.password-reset.frontend-url must use https (http is allowed only on localhost)"
        }
        require(!tokenTtl.isZero && !tokenTtl.isNegative) {
            "app.password-reset.token-ttl must be positive"
        }
        require(!requestCooldown.isNegative) {
            "app.password-reset.request-cooldown must not be negative"
        }
    }
}

@ConfigurationProperties("app.mail")
data class MailProperties(
    var from: String = "Awa <no-reply@awaroommate.ca>",
    var replyTo: String? = null,
    var resendApiKey: String = ""
)
