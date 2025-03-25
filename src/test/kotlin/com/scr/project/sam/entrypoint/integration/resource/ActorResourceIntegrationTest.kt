package com.scr.project.sam.entrypoint.integration.resource

import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.WebTestClientRestDocumentationWrapper
import com.scr.project.sam.AbstractIntegrationTest
import com.scr.project.sam.domains.actor.dao.ActorDao
import com.scr.project.sam.domains.actor.dao.bradPitt
import com.scr.project.sam.domains.actor.error.ActorExceptionHandler.ErrorResponse
import com.scr.project.sam.domains.actor.model.entity.Actor
import com.scr.project.sam.entrypoint.mapper.toApiDto
import com.scr.project.sam.entrypoint.model.api.ActorApiDto
import com.scr.project.sam.entrypoint.model.api.ActorUpdateRequestApiDto
import com.scr.project.sam.entrypoint.resource.ApiConstants.ACTOR_PATH
import com.scr.project.sam.entrypoint.resource.ApiConstants.ID_PATH
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpHeaders.CONTENT_RANGE
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.PARTIAL_CONTENT
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ListBodySpec
import java.time.LocalDate
import java.util.Locale
import kotlin.random.Random

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureRestDocs
@Disabled("Disabled while testing keycloak")
internal class ActorResourceIntegrationTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val actorDao: ActorDao,
    @Autowired private val restDocumentation: RestDocumentationContextProvider,
) : AbstractIntegrationTest() {

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp() {
        actorDao.initTestData()
    }

    private companion object {

        const val ACTOR_TAG = "Actors"
        const val GET_SUMMARY = "Find actor by id"
        const val POST_SUMMARY = "Create an actor"
        const val PATCH_SUMMARY = "Update an actor (death)"
        const val LIST_SUMMARY = "List actors"
    }

    @Test
    fun `create should succeed and create an actor`() {
        val actorRequest = ActorApiDto(
            "surname",
            "name",
            "FR",
            LocalDate.of(1980, 1, 1),
            LocalDate.of(1990, 1, 1)
        )
        val initialCount = actorDao.count()
        webTestClient.mutate().baseUrl("http://localhost:$port")
            .build()
            .post()
            .uri(ACTOR_PATH)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.writeToken}")
            .bodyValue(actorRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(ActorApiDto::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "actors-create",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(ACTOR_TAG)
                            .summary(POST_SUMMARY)
                            .description("Create an actor by entering its basic information")
                            .requestFields(
                                fieldWithPath("surname").description("Surname of the actor"),
                                fieldWithPath("name").description("Name of the actor"),
                                fieldWithPath("nationalityCode").description("Country code for the actor's nationality"),
                                fieldWithPath("birthDate").description("Birth date of the actor"),
                                fieldWithPath("deathDate").description("Death date of the actor (if applicable)").optional(),
                            )
                            .responseFields(
                                fieldWithPath("id").description("The unique identifier of the actor"),
                                fieldWithPath("surname").description("Surname of the actor"),
                                fieldWithPath("name").description("Name of the actor"),
                                fieldWithPath("nationality").description("Nationality of the actor"),
                                fieldWithPath("nationalityCode").description("Country code for the actor's nationality"),
                                fieldWithPath("birthDate").description("Birth date of the actor"),
                                fieldWithPath("deathDate").description("Death date of the actor (if applicable)").optional(),
                                fieldWithPath("isAlive").description("Indicator if the actor is alive")
                            ).build()
                    )
                )
            )
            .consumeWith {
                val body = it.responseBody as ActorApiDto
                assertThat(body).isNotNull
                with(body) {
                    assertThat(id).isNotNull
                    assertThat(surname).isEqualTo(actorRequest.surname)
                    assertThat(name).isEqualTo(actorRequest.name)
                    assertThat(nationalityCode).isEqualTo(actorRequest.nationalityCode)
                    assertThat(birthDate).isEqualTo(actorRequest.birthDate)
                    assertThat(deathDate).isEqualTo(actorRequest.deathDate)
                    assertThat(isAlive).isEqualTo(actorRequest.deathDate == null)
                    assertThat(nationality).isNotNull
                }
                assertThat(actorDao.count()).isEqualTo(initialCount + 1)
                val actor = actorDao.findById(ObjectId(body.id!!))
                with(actor!!) {
                    assertThat(id).isEqualTo(ObjectId(body.id))
                    assertThat(surname).isEqualTo(body.surname)
                    assertThat(name).isEqualTo(body.name)
                    assertThat(nationality).isEqualTo(Locale("", body.nationalityCode))
                    assertThat(birthDate).isEqualTo(body.birthDate)
                    assertThat(deathDate).isEqualTo(body.deathDate)
                }
            }
    }

    @Test
    fun `create should fail when request contains surname and name that already exist`() {
        val actorRequest = ActorApiDto(
            "Pitt",
            "Brad",
            "FR",
            LocalDate.of(1980, 1, 1),
            LocalDate.of(1990, 1, 1)
        )
        val initialCount = actorDao.count()
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .post()
            .uri(ACTOR_PATH)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.writeToken}")
            .bodyValue(actorRequest)
            .exchange()
            .expectStatus().isEqualTo(CONFLICT)
            .expectBody(ErrorResponse::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "create-conflict",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(ACTOR_TAG)
                            .summary(POST_SUMMARY)
                            .build()
                    )
                )
            )
        assertThat(actorDao.count()).isEqualTo(initialCount)
    }

    @Test
    fun `create should return 401 when authentication fails because wrong token`() {
        val actorRequest = ActorApiDto(
            "surname",
            "name",
            "FR",
            LocalDate.of(1980, 1, 1),
            LocalDate.of(1990, 1, 1)
        )
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .post()
            .uri(ACTOR_PATH)
            .header(AUTHORIZATION, "Bearer dummyToken")
            .bodyValue(actorRequest)
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody(ErrorResponse::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "create-not-authorized",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(ACTOR_TAG)
                            .summary(POST_SUMMARY)
                            .build()
                    )
                )
            )
    }

    @Test
    fun `create should return 403 when authorization fails because no matching role`() {
        val actorRequest = ActorApiDto(
            "surname",
            "name",
            "FR",
            LocalDate.of(1980, 1, 1),
            LocalDate.of(1990, 1, 1)
        )
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .post()
            .uri(ACTOR_PATH)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.standardToken}")
            .bodyValue(actorRequest)
            .exchange()
            .expectStatus().isForbidden
            .expectBody(ErrorResponse::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "create-forbidden",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(ACTOR_TAG)
                            .summary(POST_SUMMARY)
                            .build()
                    )
                )
            )
    }

    @Test
    fun `find should succeed and returns an actor response when id exists`() {
        val actorResponse = actorDao.findAny()!!.toApiDto()
        webTestClient.mutate().baseUrl("http://localhost:$port")
            .filter(documentationConfiguration(restDocumentation))
            .build()
            .get()
            .uri("$ACTOR_PATH$ID_PATH", actorResponse.id)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.standardToken}")
            .exchange()
            .expectStatus().isOk
            .expectBody(ActorApiDto::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "actors-find-by-id",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(ACTOR_TAG)
                            .summary(GET_SUMMARY)
                            .description("Retrieve the information of an actor identified by its unique id")
                            .pathParameters(
                                parameterWithName("id").description("Unique identifier of the actor")
                            )
                            .responseFields(
                                fieldWithPath("id").description("The unique identifier of the actor"),
                                fieldWithPath("surname").description("Surname of the actor"),
                                fieldWithPath("name").description("Name of the actor"),
                                fieldWithPath("nationality").description("Nationality of the actor"),
                                fieldWithPath("nationalityCode").description("Country code for the actor's nationality"),
                                fieldWithPath("birthDate").description("Birth date of the actor"),
                                fieldWithPath("deathDate").description("Death date of the actor (if applicable)").optional().type("STRING"),
                                fieldWithPath("isAlive").description("Indicator if the actor is alive")
                            ).build()
                    )
                )
            )
            .consumeWith { result ->
                val body = result.responseBody as ActorApiDto
                assertThat(body).isNotNull
                with(body) {
                    assertThat(id).isEqualTo(actorResponse.id)
                    assertThat(surname).isEqualTo(actorResponse.surname)
                    assertThat(name).isEqualTo(actorResponse.name)
                    assertThat(nationality).isEqualTo(actorResponse.nationality)
                    assertThat(nationalityCode).isEqualTo(actorResponse.nationalityCode)
                    assertThat(birthDate).isEqualTo(actorResponse.birthDate)
                    assertThat(deathDate).isEqualTo(actorResponse.deathDate)
                    assertThat(isAlive).isEqualTo(actorResponse.isAlive)
                }
            }
    }

    @Test
    fun `find should return 404 when id does not exist`() {
        webTestClient.mutate().baseUrl("http://localhost:$port")
            .filter(documentationConfiguration(restDocumentation)) // Add this if missing
            .build()
            .get()
            .uri("$ACTOR_PATH$ID_PATH", ObjectId.get().toHexString())
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.standardToken}")
            .exchange()
            .expectStatus().isNotFound
            .expectBody(ErrorResponse::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "find-not-found",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(ACTOR_TAG)
                            .summary(GET_SUMMARY)
                            .pathParameters(
                                parameterWithName("id").description("Unique identifier of the actor")
                            )
                            .responseFields(
                                fieldWithPath("errorCode").description("Error code"),
                                fieldWithPath("errorReason").description("Error reason"),
                                fieldWithPath("message").description("Error message")
                            ).build()
                    )
                )
            )
    }

    @Test
    fun `find should return 401 when authentication fails because no token in header`() {
        webTestClient.mutate().baseUrl("http://localhost:$port")
            .filter(documentationConfiguration(restDocumentation)) // Add this if missing
            .build()
            .get()
            .uri("$ACTOR_PATH$ID_PATH", ObjectId.get().toHexString())
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody(ErrorResponse::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "find-not-authorized",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(ACTOR_TAG)
                            .summary(GET_SUMMARY)
                            .pathParameters(
                                parameterWithName("id").description("Unique identifier of the actor")
                            )
                            .build()
                    )
                )
            )
    }

    @Test
    fun `patch should succeed and update actor when it exists in database`() {
        val actor = actorDao.findAnyBy { it.deathDate == null }!!
        val updateRequestDto = ActorUpdateRequestApiDto(LocalDate.now())
        val expectedOutput = actor.copy(deathDate = updateRequestDto.deathDate).toApiDto()
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .patch()
            .uri("$ACTOR_PATH$ID_PATH", actor.id)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.writeToken}")
            .bodyValue(updateRequestDto)
            .exchange()
            .expectStatus().isOk
            .expectBody(ActorApiDto::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "actor-patch",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(ACTOR_TAG)
                            .summary(PATCH_SUMMARY)
                            .description("Set a death date for an actor")
                            .pathParameters(
                                parameterWithName("id").description("Unique identifier of the actor")
                            )
                            .requestFields(
                                fieldWithPath("deathDate").description("Date of death of the actor")
                            )
                            .responseFields(
                                fieldWithPath("id").description("Id of the actor"),
                                fieldWithPath("surname").description("Surname of the actor"),
                                fieldWithPath("name").description("Name of the actor"),
                                fieldWithPath("nationality").description("Nationality of the actor"),
                                fieldWithPath("nationalityCode").description("Country code for the actor's nationality"),
                                fieldWithPath("birthDate").description("Birth date of the actor"),
                                fieldWithPath("deathDate").description("Death date of the actor"),
                                fieldWithPath("isAlive").description("Is the actor alive")
                            ).build()
                    )
                )
            )
            .consumeWith {
                val body = it.responseBody as ActorApiDto
                assertThat(body).isNotNull
                with(body) {
                    assertThat(id).isEqualTo(expectedOutput.id)
                    assertThat(surname).isEqualTo(expectedOutput.surname)
                    assertThat(name).isEqualTo(expectedOutput.name)
                    assertThat(nationality).isEqualTo(expectedOutput.nationality)
                    assertThat(birthDate).isEqualTo(expectedOutput.birthDate)
                    assertThat(deathDate).isEqualTo(expectedOutput.deathDate)
                    assertThat(isAlive).isEqualTo(expectedOutput.isAlive)
                }
                val updatedActor = actorDao.findById(actor.id!!)
                with(updatedActor!!) {
                    assertThat(id).isEqualTo(actor.id)
                    assertThat(surname).isEqualTo(actor.surname)
                    assertThat(name).isEqualTo(actor.name)
                    assertThat(nationality).isEqualTo(actor.nationality)
                    assertThat(birthDate).isEqualTo(actor.birthDate)
                    assertThat(deathDate).isEqualTo(updateRequestDto.deathDate)
                }
            }
    }

    @Test
    fun `patch should return 404 when id does not exist`() {
        val updateRequestDto = ActorUpdateRequestApiDto(LocalDate.now())
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .patch()
            .uri("$ACTOR_PATH$ID_PATH", ObjectId.get().toHexString())
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.writeToken}")
            .bodyValue(updateRequestDto)
            .exchange()
            .expectStatus().isNotFound
            .expectBody(ErrorResponse::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "patch-not-found",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(ACTOR_TAG)
                            .summary(PATCH_SUMMARY)
                            .build()
                    )
                )
            )
    }

    @Test
    fun `patch should fail when death date is in future`() {
        val actor = actorDao.findAnyBy { it.deathDate == null }!!
        val updateRequestDto = ActorUpdateRequestApiDto(LocalDate.now().plusDays(1))
        actor.copy(deathDate = updateRequestDto.deathDate).toApiDto()
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .patch()
            .uri("$ACTOR_PATH$ID_PATH", actor.id)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.writeToken}")
            .bodyValue(updateRequestDto)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Exception::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "patch-wrong-date",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(ACTOR_TAG)
                            .summary(PATCH_SUMMARY)
                            .build()
                    )
                )
            )
    }

    @Test
    fun `patch should fail when actor is already dead`() {
        val actor = actorDao.findAnyBy { it.deathDate != null }!!
        val updateRequestDto = ActorUpdateRequestApiDto(LocalDate.now())
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .patch()
            .uri("$ACTOR_PATH$ID_PATH", actor.id)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.writeToken}")
            .bodyValue(updateRequestDto)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Exception::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "patch-already-dead",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(ACTOR_TAG)
                            .summary(PATCH_SUMMARY)
                            .build()
                    )
                )
            )
    }

    @Test
    fun `patch should fail when death date is before birth date`() {
        val actor = actorDao.findAnyBy { it.deathDate == null }!!
        val updateRequestDto = ActorUpdateRequestApiDto(actor.birthDate.minusDays(1))
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .patch()
            .uri("$ACTOR_PATH$ID_PATH", actor.id)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.writeToken}")
            .bodyValue(updateRequestDto)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(Exception::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "patch-death-before-birth",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(ACTOR_TAG)
                            .summary(PATCH_SUMMARY)
                            .build()
                    )
                )
            )
    }

    @Test
    fun `patch should fail and return 401 when authentication fails`() {
        val actor = actorDao.findAnyBy { it.deathDate == null }!!
        val updateRequestDto = ActorUpdateRequestApiDto(LocalDate.now())
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .patch()
            .uri("$ACTOR_PATH$ID_PATH", actor.id)
            .header(AUTHORIZATION, "Bearer dummyToken")
            .bodyValue(updateRequestDto)
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody(ErrorResponse::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "patch-not-authorized",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(ACTOR_TAG)
                            .summary(PATCH_SUMMARY)
                            .build()
                    )
                )
            )
    }

    @Test
    fun `patch should fail and return 403 when authorization fails`() {
        val actor = actorDao.findAnyBy { it.deathDate == null }!!
        val updateRequestDto = ActorUpdateRequestApiDto(LocalDate.now())
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .patch()
            .uri("$ACTOR_PATH$ID_PATH", actor.id)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.standardToken}")
            .bodyValue(updateRequestDto)
            .exchange()
            .expectStatus().isForbidden
            .expectBody(ErrorResponse::class.java)
            .consumeWith(
                WebTestClientRestDocumentationWrapper.document(
                    "patch-forbidden",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(ACTOR_TAG)
                            .summary(PATCH_SUMMARY)
                            .build()
                    )
                )
            )
    }

    @Test
    fun `list should return all actors including the dead ones when includeDeadIndicator is true`() {
        actorDao.deleteAll()
        val actors = generateListOfActors(5)
        actorDao.insertAll(actors)
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .get()
            .uri("$ACTOR_PATH?includeDeadIndicator=true")
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.standardToken}")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(ActorApiDto::class.java)
            .consumeWith<ListBodySpec<ActorApiDto>> {
                val headers = it.responseHeaders
                assertThat(headers).isNotNull
                assertThat(headers.get(CONTENT_RANGE)).isEqualTo(listOf("actors 0-4/5"))
                val body = it.responseBody
                assertThat(body).isNotNull
                assertThat(body).hasSize(actors.size)
                body!!.forEach { actorDto ->
                    val actor = actors.find { a -> a.id!!.toHexString() == actorDto.id }
                    assertThat(actor).isNotNull
                    with(actor!!) {
                        assertThat(actorDto.id).isEqualTo(id!!.toHexString())
                        assertThat(actorDto.surname).isEqualTo(surname)
                        assertThat(actorDto.name).isEqualTo(name)
                        assertThat(actorDto.nationalityCode).isEqualTo(nationality.country)
                        assertThat(actorDto.birthDate).isEqualTo(birthDate)
                        assertThat(actorDto.deathDate).isEqualTo(deathDate)
                        assertThat(actorDto.isAlive).isEqualTo(deathDate == null)
                    }
                }
            }.consumeWith<ListBodySpec<ActorApiDto>>(
                WebTestClientRestDocumentationWrapper.document(
                    "list-full",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(ACTOR_TAG)
                            .summary(LIST_SUMMARY)
                            .description("List all registered actors with possible filtering on living ones")
                            .queryParameters(
                                parameterWithName("includeDeadIndicator").description("Indicator to include dead actors in the list")
                            )
                            .responseFields(
                                fieldWithPath("[].id").description("Id of the actor"),
                                fieldWithPath("[].surname").description("Surname of the actor"),
                                fieldWithPath("[].name").description("Name of the actor"),
                                fieldWithPath("[].nationality").description("Nationality of the actor"),
                                fieldWithPath("[].nationalityCode").description("Country code for the actor's nationality"),
                                fieldWithPath("[].birthDate").description("Birth date of the actor"),
                                fieldWithPath("[].deathDate").description("Death date of the actor").optional().type("STRING"),
                                fieldWithPath("[].isAlive").description("Is the actor alive")
                            )
                            .build()
                    )
                )
            )
    }

    @Test
    fun `list should return all actors except dead ones when no indicator explicitly provided`() {
        actorDao.deleteAll()
        val livingActor = Actor("Live", "Guy", Locale.Builder().setRegion("US").build(), LocalDate.of(1980, 1, 1))
        val deadActor = Actor("Dead", "Guy", Locale.Builder().setRegion("US").build(), LocalDate.of(1980, 1, 1), LocalDate.of(2000, 1, 1))
        val otherActors = generateListOfActors(4)
        val actors = listOf(livingActor, deadActor).plus(otherActors)
        actorDao.insertAll(actors)
        val livingActorNumber = actorDao.findAllBy { it.deathDate == null }.count()
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .get()
            .uri(ACTOR_PATH)
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.standardToken}")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(ActorApiDto::class.java)
            .consumeWith<ListBodySpec<ActorApiDto>> {
                val headers = it.responseHeaders
                assertThat(headers).isNotNull
                assertThat(headers.get(CONTENT_RANGE)).isEqualTo(listOf("actors 0-${livingActorNumber - 1}/${livingActorNumber}"))
                val body = it.responseBody
                assertThat(body).isNotNull
                assertThat(body).hasSizeLessThan(actorDao.count().toInt()).hasSizeGreaterThanOrEqualTo(1)
                body!!.forEach { actorDto ->
                    val actor = actors.find { a -> a.id!!.toHexString() == actorDto.id }
                    assertThat(actor).isNotNull
                    with(actor!!) {
                        assertThat(actorDto.id).isEqualTo(id!!.toHexString())
                        assertThat(actorDto.surname).isEqualTo(surname)
                        assertThat(actorDto.name).isEqualTo(name)
                        assertThat(actorDto.nationalityCode).isEqualTo(nationality.country)
                        assertThat(actorDto.birthDate).isEqualTo(birthDate)
                        assertThat(actorDto.deathDate).isEqualTo(deathDate)
                        assertThat(actorDto.isAlive).isEqualTo(deathDate == null)
                    }
                }
            }
    }

    @Test
    fun `list should return a subset of actors when includeDeadIndicator is true and total number is greater than default page size`() {
        actorDao.deleteAll()
        val actors = generateListOfActors(15)
        actorDao.insertAll(actors)
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .get()
            .uri("$ACTOR_PATH?includeDeadIndicator=true")
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.standardToken}")
            .exchange()
            .expectStatus().isEqualTo(PARTIAL_CONTENT)
            .expectBodyList(ActorApiDto::class.java)
            .consumeWith<ListBodySpec<ActorApiDto>> {
                val headers = it.responseHeaders
                assertThat(headers).isNotNull
                assertThat(headers.get(CONTENT_RANGE)).isEqualTo(listOf("actors 0-9/10"))
                val body = it.responseBody
                assertThat(body).isNotNull
                assertThat(body).hasSize(10)
                body!!.forEach { actorDto ->
                    val actor = actors.find { a -> a.id!!.toHexString() == actorDto.id }
                    assertThat(actor).isNotNull
                    with(actor!!) {
                        assertThat(actorDto.id).isEqualTo(id!!.toHexString())
                        assertThat(actorDto.surname).isEqualTo(surname)
                        assertThat(actorDto.name).isEqualTo(name)
                        assertThat(actorDto.nationalityCode).isEqualTo(nationality.country)
                        assertThat(actorDto.birthDate).isEqualTo(birthDate)
                        assertThat(actorDto.deathDate).isEqualTo(deathDate)
                        assertThat(actorDto.isAlive).isEqualTo(deathDate == null)
                    }
                }
            }
            .consumeWith<ListBodySpec<ActorApiDto>>(
                WebTestClientRestDocumentationWrapper.document(
                    "list-partial",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tags(ACTOR_TAG)
                            .summary(LIST_SUMMARY)
                            .queryParameters(
                                parameterWithName("includeDeadIndicator").description("Indicator to include dead actors in the list")
                            )
                            .responseFields(
                                fieldWithPath("[].id").description("Id of the actor"),
                                fieldWithPath("[].surname").description("Surname of the actor"),
                                fieldWithPath("[].name").description("Name of the actor"),
                                fieldWithPath("[].nationality").description("Nationality of the actor"),
                                fieldWithPath("[].nationalityCode").description("Country code for the actor's nationality"),
                                fieldWithPath("[].birthDate").description("Birth date of the actor"),
                                fieldWithPath("[].deathDate").description("Death date of the actor").optional().type("STRING"),
                                fieldWithPath("[].isAlive").description("Is the actor alive")
                            )
                            .build()
                    )
                )
            )
    }

    @Test
    fun `list should return an empty list when no data`() {
        actorDao.deleteAll()
        webTestClient.mutate().baseUrl("http://localhost:$port").build()
            .get()
            .uri("$ACTOR_PATH?includeDeadIndicator=true")
            .header(AUTHORIZATION, "Bearer ${testJwtUtil.standardToken}")
            .exchange()
            .expectStatus().isOk
            .expectBodyList(ActorApiDto::class.java)
            .consumeWith<ListBodySpec<ActorApiDto>> {
                val headers = it.responseHeaders
                assertThat(headers).isNotNull
                assertThat(headers.get(CONTENT_RANGE)).isEqualTo(listOf("actors */0"))
                val body = it.responseBody
                assertThat(body).isNotNull
                assertThat(body).isEmpty()
            }
            .consumeWith<ListBodySpec<ActorApiDto>>(
                WebTestClientRestDocumentationWrapper.document(
                    "list-empty",
                    preprocessResponse(prettyPrint()),
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag(ACTOR_TAG)
                            .summary(LIST_SUMMARY)
                            .build()
                    )
                )
            )
    }

    private fun generateListOfActors(size: Int): List<Actor> {
        return List(size) {
            bradPitt().copy(
                id = ObjectId.get(),
                name = generateRandomString(),
                deathDate = Random.nextBoolean().takeIf { it }?.let { bradPitt().birthDate.plusDays(10) })
        }
    }

    private fun generateRandomString(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z')
        return (1..5)
            .map { allowedChars.random() }
            .joinToString("")
    }
}