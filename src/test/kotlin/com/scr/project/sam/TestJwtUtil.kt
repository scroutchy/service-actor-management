package com.scr.project.sam

import com.scr.project.sam.AbstractIntegrationTest.Companion.keycloakContainer
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class TestJwtUtil {

    final val standardToken: String = getAccessToken("testuser", "testpass")
    final val writeToken: String = getAccessToken("writeuser", "writepass") // generateMockToken(listOf(ROLE_WRITE))

    private fun getAccessToken(username: String, password: String): String {
        val webClient = WebClient.create(keycloakContainer.authServerUrl)
        val response = webClient.post()
            .uri("/realms/my-realm/protocol/openid-connect/token")
            .contentType(APPLICATION_FORM_URLENCODED)
            .bodyValue("grant_type=password&client_id=my-client&username=$username&password=$password")
            .retrieve()
            .bodyToMono<Map<String, Any>>()
            .block()!!
        return response["access_token"] as String
    }
}