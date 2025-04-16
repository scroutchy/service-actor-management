package com.scr.project.sam.config

import com.scr.project.sam.config.TopicProperties.Companion.PREFIX
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties(PREFIX)
data class TopicProperties(
    @field:NotBlank
    val actorCreationNotification: String = "sam-rewarded-entity-creation-events"
) {

    companion object {

        const val PREFIX = "messaging.topics"
    }
}