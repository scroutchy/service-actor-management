package com.scr.project.sam

import com.scr.project.sam.AbstractIntegrationTest.Companion.keycloakContainer
import com.scr.project.sam.domains.security.config.SecurityConfiguration.Companion.ROLE_WRITE
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.Date

@Component
class TestJwtUtil(@Value("\${security.jwt.secretKey}") private val secretKey: String) {

    final val standardToken: String = getAccessToken("testuser", "testpass")
    final val writeToken: String = generateMockToken(listOf(ROLE_WRITE))

    final fun generateMockToken(roles: List<String> = listOf()): String {
        val claims = Jwts.claims().setSubject("test-user")
        claims["roles"] = roles.map { "ROLE_$it" }.toList()
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + (24 * 60 * 60 * 1000)))
            .signWith(Keys.hmacShaKeyFor(secretKey.toByteArray()))
            .compact()
    }

    private fun getAccessToken(username: String, password: String): String {
        val webClient = WebClient.create(keycloakContainer.authServerUrl)
        val response = webClient.post()
            .uri("/realms/my-realm/protocol/openid-connect/token")
            .contentType(APPLICATION_FORM_URLENCODED)
            .bodyValue("grant_type=password&client_id=my-client&username=$username&password=$password")
            .retrieve()
            .bodyToMono<Map<String, Any>>()
            .block()!!
        return response?.get("access_token") as String
    }
}