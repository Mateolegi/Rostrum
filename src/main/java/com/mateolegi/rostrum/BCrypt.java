package com.mateolegi.rostrum;

import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

import static com.mateolegi.rostrum.constant.ConfigurationFileConstants.*;

/**
 * BCrypt implements OpenBSD-style Blowfish password hashing using
 * the scheme described in "A Future-Adaptable Password Scheme" by
 * Niels Provos and David Mazieres.
 * @author <a href="mateolegi.github.io">Mateo Leal</a>
 * @version 1.0.0
 */
public class BCrypt {

    private static final int logRounds;

    static {
        JSONObject jsonObject = (JSONObject) Properties.getJSONFile().get(BCRYPT);
        logRounds = Math.toIntExact((Long) jsonObject.get(ITERATIONS));
    }

    /**
     * Hash a string using the OpenBSD bcrypt scheme
     * @param original the text to hash
     * @return hashed text
     */
    @NotNull
    public static String hash(String original) {
        return org.mindrot.jbcrypt.BCrypt.hashpw(original, org.mindrot.jbcrypt.BCrypt.gensalt(logRounds));
    }

    /**
     * Check that a plaintext matches a previously hashed
     * one
     * @param original  the plaintext password to verify
     * @param hashed    the previously-hashed password
     * @return	true if the passwords match, false otherwise
     */
    public static boolean verifyHash(String original, String hashed) {
        return org.mindrot.jbcrypt.BCrypt.checkpw(original, hashed);
    }
}
