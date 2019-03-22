package com.mateolegi.rostrum;

import com.mateolegi.rostrum.exception.PropertyNotFoundException;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

import static com.mateolegi.rostrum.constant.ConfigurationFileConstants.*;

/**
 * Represents access to the Rostrum properties file.
 * @author <a href="mateolegi.github.io">Mateo Leal</a>
 * @version 1.0.0
 */
class Properties {

    @Nullable
    public static JSONObject getJSONFile() {
        URL jsonURL = Thread.currentThread().getContextClassLoader().getResource(JSON_FILENAME);
        if (Objects.isNull(jsonURL)) {
            return null;
        }
        String jsonFile = jsonURL.getFile();
        try (Reader reader = new FileReader(jsonFile)) {
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            return null;
        }
    }

    public static JSONArray getDataSources() {
        JSONObject jsonObject = getJSONFile();
        return (JSONArray) jsonObject.get(DATA_SOURCES);
    }

    public static JSONObject getDataSource(String persistenceUnitName) {
        JSONArray dataSources = Properties.getDataSources();
        if (dataSources.isEmpty()) {
            throw new PropertyNotFoundException("No data sources found in the configuration file.");
        }
        if (Objects.isNull(persistenceUnitName)) {
            return (JSONObject) dataSources.get(0);
        }
        try {
            return (JSONObject) dataSources.stream()
                    .filter(item -> ((JSONObject) item).get(PERSISTENCE_UNIT).equals(persistenceUnitName))
                    .findFirst()
                    .orElseThrow(() -> new PropertyNotFoundException(String
                            .format("A data source with the name %s was not found", persistenceUnitName)));
        } catch (Throwable throwable) {
            throw new PropertyNotFoundException(throwable.getMessage());
        }
    }

    private static boolean isJUnitTest() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return Arrays.stream(stackTrace)
                .filter(element -> element.getClassName().startsWith("org.junit."))
                .map(element -> true)
                .findAny()
                .orElse(false);
    }
}
