package com.alltimes.cartoontime

import com.alltimes.cartoontime.utils.UUIDEncryptionUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class UUIDEncryptionUtilTest {

    private val SECRET_KEY = "yourSecretKey-16"

    @Test
    fun testUUIDEncryptionAndDecryption() {
        // Given
        val originalUUID = "THIS_IS_testUUID"

        // When
        val encryptedUUID = UUIDEncryptionUtil.encrypt(originalUUID, SECRET_KEY)
        val decryptedUUID = UUIDEncryptionUtil.decrypt(encryptedUUID, SECRET_KEY)

        // Then
        assertNotEquals("Encrypted UUID should not be the same as original UUID", originalUUID, encryptedUUID)
        assertEquals("Decrypted UUID should match the original UUID", originalUUID, decryptedUUID)
    }

    @Test
    fun testEncryptionDifferentForSameInput() {
        // Given
        val originalUUID = "123e4567-e89b-12d3-a456-426614174000"

        // When
        val encryptedUUID1 = UUIDEncryptionUtil.encrypt(originalUUID, SECRET_KEY)
        val encryptedUUID2 = UUIDEncryptionUtil.encrypt(originalUUID, SECRET_KEY)

        // Then
        assertNotEquals("Each encryption should produce different results even with the same input", encryptedUUID1, encryptedUUID2)
    }
}
