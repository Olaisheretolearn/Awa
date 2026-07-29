package com.summerlockin.Awa.exception

data class ApiErrorResponse(
    val timestamp: String = java.time.Instant.now().toString(),
    val status: Int,
    val code: String,
    val message: String,
    val requestId: String? = null,
    val errors: List<FieldErrorResponse> = emptyList()
)

data class FieldErrorResponse(
    val field: String,
    val message: String
)