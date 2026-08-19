package com.summerlockin.Awa.exception

class InvalidPasswordResetTokenException : RuntimeException(
    "The password reset link is invalid or has expired. Request a new link."
)
