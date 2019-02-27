package com.mateolegi.rostrum;

import com.mateolegi.rostrum.constant.PropertiesConstants;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesTest {

    @Test
    void getString() {
        assertNotNull(Properties.getString(PropertiesConstants.JPA_PERSISTENCE_UNIT),
                message(PropertiesConstants.JPA_PERSISTENCE_UNIT));
    }

    @Test
    void getInt() {
        assertNotNull(Properties.getInt(PropertiesConstants.BCRYPT_ITERATIONS),
                message(PropertiesConstants.BCRYPT_ITERATIONS));
    }

    private Supplier<String> message(String key) {
        return () -> "No value found for the key " + key;
    }

    @Test
    void getJSONFile() {
        JSONObject jsonObject = Properties.getJSONFile();
        assertNotNull(jsonObject);

    }
}