package com.scr.project.sam.entrypoint.unit.resource

import com.scr.project.sam.domains.actor.dao.bradPitt
import com.scr.project.sam.entrypoint.mapper.toApiDto
import com.scr.project.sam.entrypoint.model.api.ActorApiDto
import com.scr.project.sam.entrypoint.resource.toRangedResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders.ACCEPT_RANGES
import org.springframework.http.HttpHeaders.CONTENT_RANGE
import org.springframework.http.HttpStatus.OK
import org.springframework.http.HttpStatus.PARTIAL_CONTENT
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.test.test

internal class RangedResponseTest {

    @Test
    fun `to ranged response should return accept range header with collection name inferred from ApiDTO`() {
        Flux.empty<ActorApiDto>().toRangedResponse(ActorApiDto::class.java).test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.headers[ACCEPT_RANGES]!!.first()).isEqualTo("actors")
                assertThat(it.statusCode).isEqualTo(OK)
            }
            .verifyComplete()
    }

    @Test
    fun `to ranged response should return content range header with range value and ok status when returning all requested elements`() {
        actorsApiDto(5).toFlux().toRangedResponse(ActorApiDto::class.java, Pageable.ofSize(10)).test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).hasSize(5)
                assertThat(it.headers[CONTENT_RANGE]!!.first()).isEqualTo("actors 0-4/5")
                assertThat(it.statusCode).isEqualTo(OK)
            }
            .verifyComplete()
    }

    @Test
    fun `to ranged response should return content range header with range value and partial content status when returning all requested elements`() {
        actorsApiDto(5).toFlux().toRangedResponse(ActorApiDto::class.java, Pageable.ofSize(5)).test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).hasSize(5)
                assertThat(it.headers[CONTENT_RANGE]!!.first()).isEqualTo("actors 0-4/5")
                assertThat(it.statusCode).isEqualTo(PARTIAL_CONTENT)
            }
            .verifyComplete()
    }

    @Test
    fun `to ranged response with count should return content range header with count and PARTIAL_CONTENT status when returning less elements than the requested total count`() {
        actorsApiDto().toFlux().toRangedResponse(ActorApiDto::class.java, Pageable.ofSize(10)).test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).hasSize(10)
                assertThat(it.headers[CONTENT_RANGE]!!.first()).isEqualTo("actors 0-9/10")
                assertThat(it.statusCode).isEqualTo(PARTIAL_CONTENT)
            }
            .verifyComplete()
    }

    @Test
    fun `to ranged response with count should return content range header with count and partial content status when returning all elements equal to page size`() {
        actorsApiDto().toFlux().toRangedResponse(ActorApiDto::class.java, Pageable.ofSize(10)).test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).hasSize(10)
                assertThat(it.headers[CONTENT_RANGE]!!.first()).isEqualTo("actors 0-9/10")
                assertThat(it.statusCode).isEqualTo(PARTIAL_CONTENT)
            }
            .verifyComplete()
    }

    @Test
    fun `to ranged response with count should return content range header with count and partial content status when returning all elements equal to page size and total count is provided`() {
        actorsApiDto().toFlux().toRangedResponse(ActorApiDto::class.java, Pageable.ofSize(10), 30).test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).hasSize(10)
                assertThat(it.headers[CONTENT_RANGE]!!.first()).isEqualTo("actors 0-9/10")
                assertThat(it.statusCode).isEqualTo(PARTIAL_CONTENT)
            }
            .verifyComplete()
    }

    @Test
    fun `to ranged response with count should return content range header with count and OK status when returning all elements and total count is provided and smaller than page size`() {
        actorsApiDto().toFlux().toRangedResponse(ActorApiDto::class.java, Pageable.ofSize(10), 5).test()
            .expectSubscription()
            .consumeNextWith {
                assertThat(it.body).hasSize(10)
                assertThat(it.headers[CONTENT_RANGE]!!.first()).isEqualTo("actors 0-9/10")
                assertThat(it.statusCode).isEqualTo(OK)
            }
            .verifyComplete()
    }

    private fun actorsApiDto(number: Int = 10) = List(number) { bradPitt().toApiDto() }
}