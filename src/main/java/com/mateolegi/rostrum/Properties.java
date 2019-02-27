package com.mateolegi.rostrum;

import static com.mateolegi.rostrum.constant.ConfigurationFileConstants.*;
import com.mateolegi.rostrum.exception.PropertyNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Represents access to the Rostrum properties file.
 * @author <a href="mateolegi.github.io">Mateo Leal</a>
 * @version 1.0.0
 */
class Properties {

    /**
     * Name of the properties file located in the resources folder.
     */
    private static final String BUNDLE_NAME = "rostrum";

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Get the string corresponding to the given key.
     * @param name key
     * @return value
     */
    @NotNull
    public static String getString(String name) {
        return getProperty(name);
    }

    /**
     * Get the integer corresponding to the given key.
     * @param name key
     * @return value
     */
    @NotNull
    public static Integer getInt(String name) {
        return Integer.parseInt(getProperty(name));
    }

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
        JSONArray datasources = Properties.getDataSources();
        if (datasources.isEmpty()) {
            throw new PropertyNotFoundException("No data sources found in the configuration file.");
        }
        if (Objects.isNull(persistenceUnitName)) {
            return (JSONObject) datasources.get(0);
        }
        try {
            return (JSONObject) datasources.stream()
                    .filter(item -> ((JSONObject) item).get(PERSISTENCE_UNIT).equals(persistenceUnitName))
                    .findFirst()
                    .orElseThrow(() -> new PropertyNotFoundException(String
                            .format("A data source with the name %s was not found", persistenceUnitName)));
        } catch (Throwable throwable) {
            throw new PropertyNotFoundException(throwable.getMessage());
        }
    }

    @NotNull
    private static String getProperty(String name) {
        try {
            if (isJUnitTest()) {
                String testKeyName = "test." + name;
                if (BUNDLE.containsKey(testKeyName)) {
                    return BUNDLE.getString(testKeyName);
                } else {
                    Logger.getAnonymousLogger().info("No key founded to test. Using production key.");
                }
            }
            return BUNDLE.getString(name);
        } catch (NullPointerException e) {
            throw new PropertyNotFoundException();
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
