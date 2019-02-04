package com.mateolegi.rostrum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AES256Test {

    @Test
    void aes256() {
        String original = "password";
        String encrypted = AES256.encrypt(original);
        assertEquals(original, AES256.decrypt(encrypted));
    }
}