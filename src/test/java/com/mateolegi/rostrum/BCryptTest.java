package com.mateolegi.rostrum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BCryptTest {

    @Test
    void bcrypt() {
        String original = "password";
        String hash = BCrypt.hash(original);
        assertNotNull(hash);
        assertTrue(BCrypt.verifyHash(original, hash));
    }
}