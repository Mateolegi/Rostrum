package com.mateolegi.rostrum;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PropertiesTest {

    @Test
    void getJSONFile() {
        JSONObject jsonObject = Properties.getJSONFile();
        assertNotNull(jsonObject);

    }
}