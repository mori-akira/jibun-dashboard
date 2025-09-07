package com.github.moriakira.jibundashboard.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }.authorizeHttpRequests {
            it.requestMatchers("/api/v1/actuator/health").permitAll()
            it.anyRequest().authenticated()
        }.oauth2ResourceServer { it.jwt {} }
        return http.build()
    }
}

@Configuration
@Profile("dev")
class JwtDecoderConfig(
    @param:Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}") private val issuer: String,
    @param:Value("\${app.security.cognito.client-id}") private val clientId: String,
) {
    @Bean
    fun jwtDecoder(): JwtDecoder {
        val decoder = JwtDecoders.fromIssuerLocation(issuer) as NimbusJwtDecoder
        val withIssuer = JwtValidators.createDefaultWithIssuer(issuer)
        val clientIdValidator = ClientIdValidator(clientId)
        val tokenUseValidator = TokenUseValidator(expected = "access")
        decoder.setJwtValidator(DelegatingOAuth2TokenValidator(withIssuer, clientIdValidator, tokenUseValidator))
        return decoder
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter = JwtAuthenticationConverter()
}

@Configuration
@Profile("local")
class JwtDecoderLocalConfig(
    @param:Value("\${app.security.local.hs256-secret}") private val secret: String,
    @param:Value("\${app.security.local.issuer}") private val issuer: String,
    @param:Value("\${app.security.local.client-id}") private val clientId: String,
) {
    @Bean
    fun jwtDecoder(): JwtDecoder {
        val key = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
        val decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build()
        val withIssuer = JwtValidators.createDefaultWithIssuer(issuer)
        val clientValidator = ClientIdValidator(clientId)
        val tokenUseValidator = TokenUseValidator(expected = "access")
        decoder.setJwtValidator(DelegatingOAuth2TokenValidator(withIssuer, clientValidator, tokenUseValidator))
        return decoder
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter = JwtAuthenticationConverter()
}

class ClientIdValidator(private val expectedClientId: String) : OAuth2TokenValidator<Jwt> {
    override fun validate(token: Jwt): OAuth2TokenValidatorResult {
        val clientId = token.claims["client_id"] as? String
        return if (clientId == expectedClientId) {
            OAuth2TokenValidatorResult.success()
        } else {
            OAuth2TokenValidatorResult.failure(
                OAuth2Error("invalid_token", "Invalid client_id", null),
            )
        }
    }
}

class TokenUseValidator(private val expected: String) : OAuth2TokenValidator<Jwt> {
    override fun validate(token: Jwt): OAuth2TokenValidatorResult {
        val tokenUse = token.claims["token_use"] as? String
        return if (tokenUse == expected) {
            OAuth2TokenValidatorResult.success()
        } else {
            OAuth2TokenValidatorResult.failure(
                OAuth2Error("invalid_token", "token_use must be '$expected'", null),
            )
        }
    }
}
