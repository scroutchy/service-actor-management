package com.scr.project.sam.entrypoint.resource.validation

import com.scr.project.sam.entrypoint.model.api.ActorApiDto
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class DeathDateValidator : ConstraintValidator<IsValidDeathDate, ActorApiDto> {

    override fun isValid(value: ActorApiDto, context: ConstraintValidatorContext): Boolean {
        context.disableDefaultConstraintViolation()
        context.buildConstraintViolationWithTemplate("Death date must be after birth date").addConstraintViolation()
        return value.deathDate?.isAfter(value.birthDate) ?: true
    }
}