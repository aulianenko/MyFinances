package dev.aulianenko.myfinances.security

import android.util.Base64
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Utility class for encrypting and decrypting data using AES-256 encryption.
 * Uses PBKDF2 for key derivation from passwords.
 */
object EncryptionUtil {

    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_ALGORITHM = "AES"
    private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val KEY_LENGTH = 256
    private const val ITERATION_COUNT = 10000
    private const val SALT_LENGTH = 32
    private const val IV_LENGTH = 16

    /**
     * Encrypt a string using AES-256 encryption with the provided password.
     * @param data The plaintext string to encrypt
     * @param password The password to use for encryption
     * @return EncryptedData containing the encrypted data, salt, and IV
     */
    fun encrypt(data: String, password: String): EncryptedData {
        // Generate random salt
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)

        // Derive key from password
        val secretKey = deriveKey(password, salt)

        // Generate random IV
        val iv = ByteArray(IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)

        // Encrypt data
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

        // Encode to Base64 for storage
        return EncryptedData(
            data = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP),
            salt = Base64.encodeToString(salt, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    /**
     * Decrypt an encrypted string using AES-256 decryption with the provided password.
     * @param encryptedData The encrypted data containing ciphertext, salt, and IV
     * @param password The password to use for decryption
     * @return The decrypted plaintext string
     * @throws Exception if decryption fails (wrong password, corrupted data, etc.)
     */
    fun decrypt(encryptedData: EncryptedData, password: String): String {
        // Decode from Base64
        val encryptedBytes = Base64.decode(encryptedData.data, Base64.NO_WRAP)
        val salt = Base64.decode(encryptedData.salt, Base64.NO_WRAP)
        val iv = Base64.decode(encryptedData.iv, Base64.NO_WRAP)

        // Derive key from password
        val secretKey = deriveKey(password, salt)

        // Decrypt data
        val ivSpec = IvParameterSpec(iv)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Derive a secret key from a password and salt using PBKDF2.
     * @param password The password to derive the key from
     * @param salt The salt to use for key derivation
     * @return The derived SecretKey
     */
    private fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val spec: KeySpec = PBEKeySpec(
            password.toCharArray(),
            salt,
            ITERATION_COUNT,
            KEY_LENGTH
        )
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, KEY_ALGORITHM)
    }

    /**
     * Validate a password by attempting to decrypt test data.
     * @param encryptedData The encrypted data to test
     * @param password The password to validate
     * @return true if the password is correct, false otherwise
     */
    fun validatePassword(encryptedData: EncryptedData, password: String): Boolean {
        return try {
            decrypt(encryptedData, password)
            true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Data class representing encrypted data with its associated salt and IV.
 * All values are Base64-encoded strings for easy storage.
 */
data class EncryptedData(
    val data: String,
    val salt: String,
    val iv: String
)
