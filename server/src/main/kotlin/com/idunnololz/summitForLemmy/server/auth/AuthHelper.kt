package com.idunnololz.summitForLemmy.server.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.OAuthCallback
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class AuthHelper
@Inject
constructor() {
  private val algorithm = Algorithm.HMAC256(System.getProperty("jwt.access.secret"))
  private val issuer: String = System.getProperty("jwt.issuer")

  init {
    if (System.getProperty("jwt.access.secret").isNullOrBlank()) {
      error("Unable to read property 'jwt.access.secret'.")
    }
  }

  fun makeJWTVerifier(): JWTVerifier = JWT.require(algorithm)
    .withIssuer(issuer)
    .build()

  fun generateTokenPair(userId: Int): OAuthCallback.TokenPair {
    val accessToken =
      JWT.create()
        .withSubject(userId.toString())
        .withExpiresAt(Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 10))
        .withIssuer(issuer)
        .sign(algorithm)

    val refreshToken = UUID.randomUUID().toString()

    return OAuthCallback.TokenPair(accessToken, refreshToken)
  }
}
