package com.purval.folio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The synced library must round-trip with the right password and stay unreadable with any other. */
class CryptTest {
    @Test fun roundTripAndWrongKey() {
        val k = Crypt.deriveKey("correct horse battery", "user-123")
        val blob = Crypt.encrypt(k, """{"xp":420}""")
        assertNotEquals(blob, Crypt.encrypt(k, """{"xp":420}""")) // fresh IV each time
        assertEquals("""{"xp":420}""", Crypt.decrypt(k, blob))
        assertNull(Crypt.decrypt(Crypt.deriveKey("wrong password", "user-123"), blob))
        assertNull(Crypt.decrypt(Crypt.deriveKey("correct horse battery", "user-999"), blob))
    }
}
