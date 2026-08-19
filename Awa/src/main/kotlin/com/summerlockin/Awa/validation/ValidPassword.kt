package com.summerlockin.Awa.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

const val PASSWORD_MIN_CHARACTERS = 12
const val PASSWORD_MAX_CHARACTERS = 72
const val PASSWORD_MAX_UTF8_BYTES = 72
const val PASSWORD_VALIDATION_MESSAGE =
    "Password must be 12 to 72 characters and no more than 72 UTF-8 bytes"

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PasswordConstraintValidator::class])
annotation class ValidPassword(
    val message: String = PASSWORD_VALIDATION_MESSAGE,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class PasswordConstraintValidator : ConstraintValidator<ValidPassword, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        return value.length in PASSWORD_MIN_CHARACTERS..PASSWORD_MAX_CHARACTERS &&
            value.toByteArray(Charsets.UTF_8).size <= PASSWORD_MAX_UTF8_BYTES
    }
}
