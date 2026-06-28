package ru.sapozhnikov.aiagent.data.remote.mcp.oauth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

internal object McpPkce {
    private const val CODE_VERIFIER_LENGTH = 64
    private const val CHALLENGE_METHOD = "S256"

    data class Challenge(
        val codeVerifier: String,
        val codeChallenge: String,
        val codeChallengeMethod: String = CHALLENGE_METHOD,
    )

    fun createChallenge(): Challenge {
        val codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier)
        return Challenge(
            codeVerifier = codeVerifier,
            codeChallenge = codeChallenge,
        )
    }

    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(CODE_VERIFIER_LENGTH)
        SecureRandom().nextBytes(bytes)
        return base64UrlEncode(bytes)
    }

    private fun generateCodeChallenge(codeVerifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray(Charsets.US_ASCII))
        return base64UrlEncode(digest)
    }

    private fun base64UrlEncode(bytes: ByteArray): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
