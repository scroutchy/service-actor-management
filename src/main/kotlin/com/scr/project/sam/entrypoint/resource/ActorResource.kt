package com.scr.project.sam.entrypoint.resource

import com.scr.project.sam.domains.actor.service.ActorService
import com.scr.project.sam.entrypoint.mapper.toApiDto
import com.scr.project.sam.entrypoint.mapper.toEntity
import com.scr.project.sam.entrypoint.mapper.toUpdateRequest
import com.scr.project.sam.entrypoint.model.api.ActorApiDto
import com.scr.project.sam.entrypoint.model.api.ActorUpdateRequestApiDto
import com.scr.project.sam.entrypoint.resource.ApiConstants.ACTOR_PATH
import com.scr.project.sam.entrypoint.resource.ApiConstants.ID_PATH
import com.scr.project.sam.entrypoint.resource.validation.ValidationGroups.ActorRequest
import jakarta.validation.Valid
import jakarta.validation.groups.Default
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.support.PagedListHolder.DEFAULT_PAGE_SIZE
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(ACTOR_PATH)
class ActorResource(val actorService: ActorService) {

    private val logger: Logger = LoggerFactory.getLogger(ActorResource::class.java)

    @PostMapping
    fun create(@RequestBody @Validated(Default::class, ActorRequest::class) request: ActorApiDto): Mono<ResponseEntity<ActorApiDto>> {
        return actorService.create(request.toEntity())
            .map { it.toApiDto() }
            .map { ResponseEntity(it, CREATED) }
            .doOnSubscribe { logger.debug("Creation request received") }
            .doOnSuccess { logger.info("Creation request successfully handled") }
    }

    @GetMapping(ID_PATH)
    fun find(@PathVariable id: ObjectId): Mono<ActorApiDto> {
        return actorService.findById(id)
            .map { it.toApiDto() }
            .doOnSubscribe { logger.debug("Find request received") }
            .doOnSuccess { logger.debug("Finding actor request with id {${it.id}} successfully handled") }
    }

    @PatchMapping(ID_PATH)
    fun patch(@PathVariable id: ObjectId, @RequestBody @Valid request: ActorUpdateRequestApiDto): Mono<ActorApiDto> {
        return actorService.update(request.toUpdateRequest(id))
            .map { it.toApiDto() }
            .doOnSubscribe { logger.debug("Update request received") }
            .doOnSuccess { logger.info("Update request for actor with id {${it.id}} successfully handled") }
            .doOnError { logger.warn("Error at processing update request for actor with id {${id}}") }
    }

    @GetMapping
    fun list(
        @RequestParam includeDeadIndicator: Boolean = false,
        @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = ["surname"], direction = ASC) pageable: Pageable
    ): RangedResponse<ActorApiDto> {
        return actorService.findAll(includeDeadIndicator, pageable)
            .map { it.toApiDto() }
            .toRangedResponse(ActorApiDto::class.java, pageable)
            .doOnSubscribe { logger.debug("List request received") }
            .doOnSuccess { logger.debug("Listing actors request successfully handled") }
            .doOnError { logger.warn("Error at processing list request") }
    }
}