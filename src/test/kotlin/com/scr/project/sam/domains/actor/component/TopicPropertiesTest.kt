package com.scr.project.sam.domains.actor.component

import com.scr.project.sam.AbstractIntegrationTest
import com.scr.project.sam.config.TopicProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(properties = ["messaging.topics.actorCreationNotification=test-topic"])
@EnableConfigurationProperties(TopicProperties::class)
class TopicPropertiesTest(@Autowired private val topicProperties: TopicProperties) : AbstractIntegrationTest() {

    @Test
    fun `topicProperties should load properties from configuration`() {
        assertThat(topicProperties.actorCreationNotification).isEqualTo("test-topic")
    }
}