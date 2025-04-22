package com.scr.project.sam.domains.outbox.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.scr.project.sam.domains.outbox.model.entity.Outbox
import com.scr.project.sam.domains.outbox.repository.OutboxRepository
import com.scr.project.srm.RewardedKafkaDto
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.apache.kafka.clients.producer.RecordMetadata
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kafka.sender.SenderResult
import reactor.kotlin.core.publisher.toFlux

class OutboxRelayerServiceTest {

    private val outboxRepository = mockk<OutboxRepository>()
    private val kafkaSender = mockk<KafkaSender<String, RewardedKafkaDto>>()
    private val objectMapper = ObjectMapper()
    private val outboxRelayerService = OutboxRelayerService(outboxRepository, kafkaSender, objectMapper)
    private val outbox = Outbox(RewardedKafkaDto::class.java.name, "id", "{ \"id\": \"value\", \"type\": \"ACTOR\" }", "topic")
    private val senderRecordSlot = slot<Mono<SenderRecord<String, RewardedKafkaDto, ObjectId>>>()

    @BeforeEach
    fun setUp() {
        clearMocks(outboxRepository, kafkaSender)
        every { kafkaSender.send(capture(senderRecordSlot)) } answers {
            val capturedRecord = senderRecordSlot.captured.block() // Blocking is acceptable in tests
            val correlationId = capturedRecord?.correlationMetadata()
            val mockRecordMetadata = mockk<RecordMetadata>()
            every { mockRecordMetadata.offset() } returns 123L // Example offset
            every { mockRecordMetadata.partition() } returns 456
            val mockSenderResult = mockk<SenderResult<ObjectId>>()
            every { mockSenderResult.correlationMetadata() } returns correlationId
            every { mockSenderResult.recordMetadata() } returns mockRecordMetadata
            every { mockSenderResult.exception() } returns null // Indicate no exception
            Flux.just(mockSenderResult)
        }
    }

    @Test
    fun `processOutbox should succeed when outbox is empty`() {
        every { outboxRepository.findAll() } answers { Flux.empty() }
        outboxRelayerService.processOutbox()
        verify(exactly = 1) { outboxRepository.findAll() }
        verify(inverse = true) { kafkaSender.send<Publisher<SenderRecord<String, RewardedKafkaDto, String>>>(any()) }
        verify(inverse = true) { outboxRepository.delete(any()) }
        confirmVerified(outboxRepository, kafkaSender)
    }

    @Test
    fun `processOutbox should succeed when outbox contains one document`() {
        every { outboxRepository.findAll() } answers { listOf(outbox).toFlux() }
        every { outboxRepository.delete(outbox) } answers { Mono.empty() }
        outboxRelayerService.processOutbox()
        verify(exactly = 1) { outboxRepository.findAll() }
        verify(exactly = 1) { kafkaSender.send(any<Mono<SenderRecord<String, RewardedKafkaDto, ObjectId>>>()) }
        verify(exactly = 1) { outboxRepository.delete(outbox) }
        confirmVerified(outboxRepository, kafkaSender)
    }

    @Test
    fun `processOutbox should handle kafka exception and not delete outbox record`() {
        every { outboxRepository.findAll() } answers { listOf(outbox).toFlux() }
        every { kafkaSender.send(any<Mono<SenderRecord<String, RewardedKafkaDto, ObjectId>>>()) } answers {
            Flux.error(RuntimeException("Kafka send failed"))
        }
        outboxRelayerService.processOutbox()
        verify(exactly = 1) { outboxRepository.findAll() }
        verify(exactly = 1) { kafkaSender.send(any<Mono<SenderRecord<String, RewardedKafkaDto, ObjectId>>>()) }
        verify(inverse = true) { outboxRepository.delete(outbox) }
        confirmVerified(outboxRepository, kafkaSender)
    }

    @Test
    fun `processOutbox should handle when deletion of outbox fails`() {
        every { outboxRepository.findAll() } answers { listOf(outbox).toFlux() }
        every { outboxRepository.delete(outbox) } answers { Mono.error(RuntimeException("Delete failed")) }
        outboxRelayerService.processOutbox()
        verify(exactly = 1) { outboxRepository.findAll() }
        verify(exactly = 1) { kafkaSender.send(any<Mono<SenderRecord<String, RewardedKafkaDto, ObjectId>>>()) }
        verify(exactly = 1) { outboxRepository.delete(outbox) }
        confirmVerified(outboxRepository, kafkaSender)
    }

    @Test
    fun `processOutbox should succeed when more than one document in outbox and filter out outbox events that are not RewardedKafkaDto`() {
        val outbox1 = Outbox(RewardedKafkaDto::class.java.name, "id", "{ \"id\": \"value1\", \"type\": \"MOVIE\" }", "topic")
        val outbox2 = Outbox("dummy", "id", "{ \"id\": \"value1\", \"type\": \"MOVIE\" }", "topic")
        every { outboxRepository.findAll() } answers { listOf(outbox, outbox1).toFlux() }
        every { outboxRepository.delete(any<Outbox>()) } answers { Mono.empty() }
        outboxRelayerService.processOutbox()
        verify(exactly = 1) { outboxRepository.findAll() }
        verify(exactly = 2) { kafkaSender.send(any<Mono<SenderRecord<String, RewardedKafkaDto, ObjectId>>>()) }
        verify(exactly = 1) { outboxRepository.delete(outbox) }
        verify(exactly = 1) { outboxRepository.delete(outbox1) }
        verify(inverse = true) { outboxRepository.delete(outbox2) }
        confirmVerified(outboxRepository, kafkaSender)
    }
}