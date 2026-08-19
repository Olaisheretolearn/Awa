package com.summerlockin.Awa.service

import org.springframework.stereotype.Component
import org.springframework.web.util.HtmlUtils

data class PasswordResetEmail(
    val subject: String,
    val html: String,
    val text: String
)

@Component
class PasswordResetEmailTemplate {
    fun render(firstName: String, resetLink: String, expiresInMinutes: Long): PasswordResetEmail {
        val safeName = HtmlUtils.htmlEscape(firstName)
        val safeLink = HtmlUtils.htmlEscape(resetLink)
        val greeting = firstName.trim().takeIf { it.isNotEmpty() }?.let { "Hi $it," } ?: "Hello,"

        return PasswordResetEmail(
            subject = "Reset your Awa password",
            html = """
                <!doctype html>
                <html lang="en">
                  <body style="margin:0;background:#f5f7f6;font-family:Arial,sans-serif;color:#18332b;">
                    <div style="max-width:560px;margin:0 auto;padding:40px 20px;">
                      <div style="background:#ffffff;border-radius:16px;padding:32px;border:1px solid #e3e9e6;">
                        <div style="font-size:24px;font-weight:700;margin-bottom:24px;">Awa</div>
                        <h1 style="font-size:24px;line-height:1.3;margin:0 0 16px;">Reset your password</h1>
                        <p style="font-size:16px;line-height:1.6;margin:0 0 12px;">Hi $safeName,</p>
                        <p style="font-size:16px;line-height:1.6;margin:0 0 24px;">
                          We received a request to reset your Awa password. This link expires in $expiresInMinutes minutes.
                        </p>
                        <p style="margin:0 0 24px;">
                          <a href="$safeLink" style="display:inline-block;background:#176b52;color:#ffffff;text-decoration:none;font-weight:700;padding:13px 20px;border-radius:10px;">
                            Reset password
                          </a>
                        </p>
                        <p style="font-size:14px;line-height:1.6;color:#52655f;margin:0 0 12px;">
                          If the button does not work, copy and paste this link into your browser:<br>
                          <a href="$safeLink" style="color:#176b52;word-break:break-all;">$safeLink</a>
                        </p>
                        <p style="font-size:14px;line-height:1.6;color:#52655f;margin:0;">
                          If you did not request this, you can ignore this email. Your password has not changed.
                        </p>
                      </div>
                    </div>
                  </body>
                </html>
            """.trimIndent(),
            text = """
                $greeting

                We received a request to reset your Awa password.

                Reset it here: $resetLink

                This link expires in $expiresInMinutes minutes. If you did not request this, ignore this email.
            """.trimIndent()
        )
    }
}
