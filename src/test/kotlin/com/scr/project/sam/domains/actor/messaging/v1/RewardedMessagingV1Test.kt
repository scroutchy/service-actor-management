package com.scr.project.sam.domains.actor.messaging.v1

import com.scr.project.sam.config.TopicProperties
import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.srm.RewardedKafkaDto
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kotlin.test.test
import java.time.LocalDate
import java.util.Locale

class RewardedMessagingV1Test {

    private val kafkaSender = mockk<KafkaSender<String, RewardedKafkaDto>>()
    private val topicProperties = TopicProperties()
    private val rewardedMessagingV1 = RewardedMessagingV1(kafkaSender, topicProperties)

    @BeforeEach
    fun setUp() {
        clearMocks(kafkaSender)
    }

    @Test
    fun `notify should succeed`() {
        val actor = Actor("surname", "name", Locale.Builder().setRegion("FR").build(), LocalDate.now())
        every {
            kafkaSender.send(any<Mono<SenderRecord<String, RewardedKafkaDto, Nothing?>>>())
        } answers { Flux.just() }
        rewardedMessagingV1.notify(actor)
            .test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it).isEqualTo(actor)
            }
            .verifyComplete()
        verify(exactly = 1) { kafkaSender.send(any<Mono<SenderRecord<String, RewardedKafkaDto, Nothing?>>>()) }
        confirmVerified(kafkaSender)
    }

    @Test
    fun `notify should handle error when kafkaSender send fails`() {
        val actor = Actor("surname", "name", Locale.Builder().setRegion("FR").build(), LocalDate.now())
        val error = RuntimeException("Kafka send error")
        every {
            kafkaSender.send(any<Mono<SenderRecord<String, RewardedKafkaDto, Nothing?>>>())
        } answers { Flux.error(error) }

        rewardedMessagingV1.notify(actor)
            .test()
            .expectSubscription()
            .expectErrorSatisfies { assertThat(it).isInstanceOf(RuntimeException::class.java) }
            .verify()

        verify(exactly = 1) { kafkaSender.send(any<Mono<SenderRecord<String, RewardedKafkaDto, Nothing?>>>()) }
        confirmVerified(kafkaSender)
    }
}