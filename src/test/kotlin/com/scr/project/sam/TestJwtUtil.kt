package com.scr.project.sam

import com.scr.project.sam.domains.security.config.SecurityConfiguration.Companion.ROLE_WRITE
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class TestJwtUtil(@Value("\${security.jwt.secretKey}") private val secretKey: String) {

    final val standardToken: String = generateMockToken()
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
}