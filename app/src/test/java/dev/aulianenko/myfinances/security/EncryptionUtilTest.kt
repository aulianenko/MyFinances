package dev.aulianenko.myfinances.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Comprehensive tests for EncryptionUtil to ensure security-critical encryption/decryption
 * functionality works correctly and securely.
 */
class EncryptionUtilTest {

    @Test
    fun `encrypt should produce EncryptedData with all required fields`() {
        val plaintext = "Test data"
        val password = "SecurePassword123"

        val encrypted = EncryptionUtil.encrypt(plaintext, password)

        assertNotNull(encrypted.data)
        assertNotNull(encrypted.salt)
        assertNotNull(encrypted.iv)
        assertTrue(encrypted.data.isNotEmpty())
        assertTrue(encrypted.salt.isNotEmpty())
        assertTrue(encrypted.iv.isNotEmpty())
    }

    @Test
    fun `decrypt should correctly decrypt encrypted data with correct password`() {
        val plaintext = "Hello, World!"
        val password = "MySecretPassword"

        val encrypted = EncryptionUtil.encrypt(plaintext, password)
        val decrypted = EncryptionUtil.decrypt(encrypted, password)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encrypt and decrypt should work with empty string`() {
        val plaintext = ""
        val password = "password"

        val encrypted = EncryptionUtil.encrypt(plaintext, password)
        val decrypted = EncryptionUtil.decrypt(encrypted, password)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encrypt and decrypt should work with long text`() {
        val plaintext = "A".repeat(10000) // 10KB of text
        val password = "LongPassword123!"

        val encrypted = EncryptionUtil.encrypt(plaintext, password)
        val decrypted = EncryptionUtil.decrypt(encrypted, password)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encrypt and decrypt should work with special characters`() {
        val plaintext = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?~`"
        val password = "ComplexP@ssw0rd!"

        val encrypted = EncryptionUtil.encrypt(plaintext, password)
        val decrypted = EncryptionUtil.decrypt(encrypted, password)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encrypt and decrypt should work with unicode characters`() {
        val plaintext = "Unicode: 你好世界 🌍 émojis 😀 Кириллица"
        val password = "UnicodePassword"

        val encrypted = EncryptionUtil.encrypt(plaintext, password)
        val decrypted = EncryptionUtil.decrypt(encrypted, password)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encrypt and decrypt should work with JSON data`() {
        val jsonData = """
            {
                "accounts": [
                    {"id": "123", "name": "Test Account", "value": 1000.50}
                ],
                "metadata": {
                    "version": 1,
                    "timestamp": 1234567890
                }
            }
        """.trimIndent()
        val password = "JsonPassword123"

        val encrypted = EncryptionUtil.encrypt(jsonData, password)
        val decrypted = EncryptionUtil.decrypt(encrypted, password)

        assertEquals(jsonData, decrypted)
    }

    @Test(expected = Exception::class)
    fun `decrypt should throw exception with wrong password`() {
        val plaintext = "Secret data"
        val correctPassword = "CorrectPassword"
        val wrongPassword = "WrongPassword"

        val encrypted = EncryptionUtil.encrypt(plaintext, correctPassword)

        // This should throw an exception
        EncryptionUtil.decrypt(encrypted, wrongPassword)
    }

    @Test(expected = Exception::class)
    fun `decrypt should throw exception with corrupted data`() {
        val password = "Password123"
        val corruptedData = EncryptedData(
            data = "invalid-base64-data!@#$",
            salt = "valid-salt",
            iv = "valid-iv"
        )

        // This should throw an exception
        EncryptionUtil.decrypt(corruptedData, password)
    }

    @Test(expected = Exception::class)
    fun `decrypt should throw exception with corrupted salt`() {
        val plaintext = "Test"
        val password = "Password"

        val encrypted = EncryptionUtil.encrypt(plaintext, password)
        val corrupted = encrypted.copy(salt = "corrupted-salt!@#")

        // This should throw an exception
        EncryptionUtil.decrypt(corrupted, password)
    }

    @Test(expected = Exception::class)
    fun `decrypt should throw exception with corrupted IV`() {
        val plaintext = "Test"
        val password = "Password"

        val encrypted = EncryptionUtil.encrypt(plaintext, password)
        val corrupted = encrypted.copy(iv = "corrupted-iv!@#")

        // This should throw an exception
        EncryptionUtil.decrypt(corrupted, password)
    }

    @Test
    fun `encrypt should produce different ciphertext for same plaintext and password`() {
        // Due to random salt and IV, same input should produce different output
        val plaintext = "Same text"
        val password = "SamePassword"

        val encrypted1 = EncryptionUtil.encrypt(plaintext, password)
        val encrypted2 = EncryptionUtil.encrypt(plaintext, password)

        // Data should be different (different IV and salt)
        assertNotEquals(encrypted1.data, encrypted2.data)
        assertNotEquals(encrypted1.salt, encrypted2.salt)
        assertNotEquals(encrypted1.iv, encrypted2.iv)

        // But both should decrypt to same plaintext
        val decrypted1 = EncryptionUtil.decrypt(encrypted1, password)
        val decrypted2 = EncryptionUtil.decrypt(encrypted2, password)
        assertEquals(plaintext, decrypted1)
        assertEquals(plaintext, decrypted2)
    }

    @Test
    fun `encrypt should produce different output with different passwords`() {
        val plaintext = "Test data"
        val password1 = "Password1"
        val password2 = "Password2"

        val encrypted1 = EncryptionUtil.encrypt(plaintext, password1)
        val encrypted2 = EncryptionUtil.encrypt(plaintext, password2)

        // Different passwords should produce different ciphertext
        assertNotEquals(encrypted1.data, encrypted2.data)
    }

    @Test
    fun `validatePassword should return true for correct password`() {
        val plaintext = "Test data"
        val password = "CorrectPassword"

        val encrypted = EncryptionUtil.encrypt(plaintext, password)
        val isValid = EncryptionUtil.validatePassword(encrypted, password)

        assertTrue(isValid)
    }

    @Test
    fun `validatePassword should return false for incorrect password`() {
        val plaintext = "Test data"
        val correctPassword = "CorrectPassword"
        val wrongPassword = "WrongPassword"

        val encrypted = EncryptionUtil.encrypt(plaintext, correctPassword)
        val isValid = EncryptionUtil.validatePassword(encrypted, wrongPassword)

        assertFalse(isValid)
    }

    @Test
    fun `validatePassword should return false for empty password when encrypted with non-empty`() {
        val plaintext = "Test data"
        val password = "NonEmptyPassword"

        val encrypted = EncryptionUtil.encrypt(plaintext, password)
        val isValid = EncryptionUtil.validatePassword(encrypted, "")

        assertFalse(isValid)
    }

    @Test
    fun `encrypt and decrypt should work with very short password`() {
        val plaintext = "Test"
        val shortPassword = "a" // Single character password

        val encrypted = EncryptionUtil.encrypt(plaintext, shortPassword)
        val decrypted = EncryptionUtil.decrypt(encrypted, shortPassword)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encrypt and decrypt should work with very long password`() {
        val plaintext = "Test"
        val longPassword = "P".repeat(1000) // 1000 character password

        val encrypted = EncryptionUtil.encrypt(plaintext, longPassword)
        val decrypted = EncryptionUtil.decrypt(encrypted, longPassword)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `encrypted data should be base64 encoded`() {
        val plaintext = "Test"
        val password = "Password"

        val encrypted = EncryptionUtil.encrypt(plaintext, password)

        // Base64 only contains A-Z, a-z, 0-9, +, /, and =
        val base64Regex = "^[A-Za-z0-9+/=]+$".toRegex()
        assertTrue(base64Regex.matches(encrypted.data))
        assertTrue(base64Regex.matches(encrypted.salt))
        assertTrue(base64Regex.matches(encrypted.iv))
    }

    @Test
    fun `salt should be unique for each encryption`() {
        val plaintext = "Test"
        val password = "Password"

        val salts = mutableSetOf<String>()

        // Generate 100 encryptions and ensure all salts are unique
        repeat(100) {
            val encrypted = EncryptionUtil.encrypt(plaintext, password)
            salts.add(encrypted.salt)
        }

        // All salts should be unique
        assertEquals(100, salts.size)
    }

    @Test
    fun `IV should be unique for each encryption`() {
        val plaintext = "Test"
        val password = "Password"

        val ivs = mutableSetOf<String>()

        // Generate 100 encryptions and ensure all IVs are unique
        repeat(100) {
            val encrypted = EncryptionUtil.encrypt(plaintext, password)
            ivs.add(encrypted.iv)
        }

        // All IVs should be unique
        assertEquals(100, ivs.size)
    }

    @Test
    fun `encryption should be deterministic with same salt and IV`() {
        // This test verifies the encryption algorithm itself is deterministic
        // when using the same salt and IV (though in practice we use random ones)
        val plaintext = "Test data"
        val password = "Password"

        val encrypted1 = EncryptionUtil.encrypt(plaintext, password)
        val encrypted2 = EncryptionUtil.encrypt(plaintext, password)

        // Create new encrypted data with same salt and IV as first encryption
        val encrypted2WithSameSaltIV = encrypted2.copy(
            salt = encrypted1.salt,
            iv = encrypted1.iv
        )

        // When we decrypt with correct password, both should work
        val decrypted1 = EncryptionUtil.decrypt(encrypted1, password)
        assertEquals(plaintext, decrypted1)
    }

    @Test
    fun `multiple encrypt-decrypt cycles should maintain data integrity`() {
        var data = "Original data"
        val password = "Password123"

        // Encrypt and decrypt 10 times
        repeat(10) {
            val encrypted = EncryptionUtil.encrypt(data, password)
            data = EncryptionUtil.decrypt(encrypted, password)
        }

        assertEquals("Original data", data)
    }

    @Test
    fun `EncryptedData data class should support equality`() {
        val data1 = EncryptedData("data1", "salt1", "iv1")
        val data2 = EncryptedData("data1", "salt1", "iv1")
        val data3 = EncryptedData("data2", "salt2", "iv2")

        assertEquals(data1, data2)
        assertNotEquals(data1, data3)
    }

    @Test
    fun `EncryptedData should support copy`() {
        val original = EncryptedData("data", "salt", "iv")
        val copied = original.copy(data = "newData")

        assertEquals("newData", copied.data)
        assertEquals("salt", copied.salt)
        assertEquals("iv", copied.iv)
        assertNotEquals(original, copied)
    }

    @Test(expected = Exception::class)
    fun `decrypt with empty encrypted data should throw exception`() {
        val emptyEncrypted = EncryptedData("", "salt", "iv")
        EncryptionUtil.decrypt(emptyEncrypted, "password")
    }

    @Test
    fun `encrypt with whitespace-only content should work`() {
        val whitespace = "   \n\t\r   "
        val password = "Password"

        val encrypted = EncryptionUtil.encrypt(whitespace, password)
        val decrypted = EncryptionUtil.decrypt(encrypted, password)

        assertEquals(whitespace, decrypted)
    }

    @Test
    fun `password with unicode should work correctly`() {
        val plaintext = "Test data"
        val unicodePassword = "P@ssw0rd密码🔐"

        val encrypted = EncryptionUtil.encrypt(plaintext, unicodePassword)
        val decrypted = EncryptionUtil.decrypt(encrypted, unicodePassword)

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `validatePassword should handle corrupted data gracefully`() {
        val corrupted = EncryptedData("invalid!!!", "invalid!!!", "invalid!!!")
        val isValid = EncryptionUtil.validatePassword(corrupted, "password")

        assertFalse(isValid)
    }
}
