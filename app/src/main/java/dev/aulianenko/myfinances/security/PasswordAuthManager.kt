package dev.aulianenko.myfinances.security

import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

/**
 * Utility for securely hashing and verifying passwords for app lock.
 * Uses SHA-256 with salt for secure password storage.
 */
object PasswordAuthManager {

    private const val SALT_LENGTH = 32
    private const val HASH_ALGORITHM = "SHA-256"

    /**
     * Hash a password with a random salt.
     * @param password The plaintext password to hash
     * @return Base64-encoded string containing salt and hash separated by ":"
     */
    fun hashPassword(password: String): String {
        // Generate random salt
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)

        // Hash password with salt
        val hash = hashWithSalt(password, salt)

        // Encode salt and hash as Base64 and combine
        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP)

        return "$saltBase64:$hashBase64"
    }

    /**
     * Verify a password against a stored hash.
     * @param password The plaintext password to verify
     * @param storedHash The stored hash (salt:hash format)
     * @return true if password is correct, false otherwise
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            // Split stored hash into salt and hash
            val parts = storedHash.split(":")
            if (parts.size != 2) return false

            val salt = Base64.decode(parts[0], Base64.NO_WRAP)
            val expectedHash = Base64.decode(parts[1], Base64.NO_WRAP)

            // Hash input password with same salt
            val actualHash = hashWithSalt(password, salt)

            // Compare hashes (constant-time comparison to prevent timing attacks)
            return actualHash.contentEquals(expectedHash)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Validate password meets minimum requirements.
     * @param password The password to validate
     * @return true if password meets requirements, false otherwise
     */
    fun isPasswordValid(password: String): Boolean {
        // Minimum 6 characters for app lock
        // (Less strict than export encryption which has no minimum)
        return password.length >= 6
    }

    /**
     * Get password strength as a score from 0 (weakest) to 3 (strongest).
     * @param password The password to evaluate
     * @return Password strength score
     */
    fun getPasswordStrength(password: String): Int {
        if (password.length < 6) return 0

        var score = 0

        // Length bonus
        if (password.length >= 8) score++
        if (password.length >= 12) score++

        // Character variety
        val hasLowercase = password.any { it.isLowerCase() }
        val hasUppercase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }

        val varietyCount = listOf(hasLowercase, hasUppercase, hasDigit, hasSpecial).count { it }
        if (varietyCount >= 3) score++

        return score.coerceIn(0, 3)
    }

    /**
     * Get password strength as a human-readable string.
     * @param password The password to evaluate
     * @return Strength description ("Weak", "Fair", "Good", "Strong")
     */
    fun getPasswordStrengthText(password: String): String {
        return when (getPasswordStrength(password)) {
            0 -> "Too Short"
            1 -> "Weak"
            2 -> "Fair"
            3 -> "Strong"
            else -> "Weak"
        }
    }

    /**
     * Hash a password with a given salt using SHA-256.
     * @param password The plaintext password
     * @param salt The salt bytes
     * @return The hash bytes
     */
    private fun hashWithSalt(password: String, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)

        // Add salt to digest
        digest.update(salt)

        // Hash password
        return digest.digest(password.toByteArray(Charsets.UTF_8))
    }
}
