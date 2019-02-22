package com.mateolegi.rostrum;

import com.mateolegi.rostrum.exception.PropertyNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
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
