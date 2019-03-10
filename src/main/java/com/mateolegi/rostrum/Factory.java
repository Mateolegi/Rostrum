package com.mateolegi.rostrum;

import com.mateolegi.rostrum.constant.ConfigurationFileConstants;
import com.mateolegi.rostrum.constant.DatabaseProvider;
import org.eclipse.persistence.config.TargetServer;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.json.simple.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.persistence.config.PersistenceUnitProperties.*;

/**
 * Responsible for managing the connections to the persistence context.
 * @author <a href="mateolegi.github.io">Mateo Leal</a>
 * @version 1.0.0
 */
public class Factory {

    private static final Map<String, EntityManagerFactory> FACTORIES = new HashMap<>();

    /**
     * Generates EntityManager to interact with the persistence context.
     * @return generated EntityManager
     */
    public static synchronized EntityManager getEntityManager(String persistenceUnit) {
        return getEntityManagerFactory(persistenceUnit).createEntityManager();
    }

    public static EntityManagerFactory getEntityManagerFactory(String persistenceUnit) {
        if (!FACTORIES.containsKey(persistenceUnit)) {
            createEntityManagerFactory(persistenceUnit);
        }
        return FACTORIES.get(persistenceUnit);
    }

    private static void createEntityManagerFactory(String persistenceUnit) {
        FACTORIES.put(persistenceUnit, new PersistenceProvider()
                .createEntityManagerFactory(persistenceUnit, getProperties(persistenceUnit)));
    }

    private static Map<String, String> getProperties(String persistenceUnit) {
        JSONObject datasource = Properties.getDataSource(persistenceUnit);
        Map<String, String> properties = new HashMap<>();
        // Ensure RESOURCE_LOCAL transactions is used.
        properties.put(TRANSACTION_TYPE, PersistenceUnitTransactionType.RESOURCE_LOCAL.name());
        // Internal connection pool
        setDriver(datasource, properties);
        setURL(datasource, properties);
        setUser(datasource, properties);
        setPassword(datasource, properties);
        // Configure logging. FINE ensures all SQL is shown
        properties.put(LOGGING_LEVEL, "FINE");
        properties.put(LOGGING_TIMESTAMP, "false");
        properties.put(LOGGING_THREAD, "false");
        properties.put(LOGGING_SESSION, "false");
        // Ensure that no server-platform is configured
        properties.put(TARGET_SERVER, TargetServer.None);
        return properties;
    }

    private static void setDriver(JSONObject datasource, Map<String, String> properties) {
        String driverClass;
        if (datasource.containsKey(ConfigurationFileConstants.DATABASE_DRIVER)) {
            driverClass = (String) datasource.get(ConfigurationFileConstants.DATABASE_DRIVER);
        } else {
            String dbConnection = (String) datasource.get(ConfigurationFileConstants.DATABASE_CONNECTION);
            driverClass = DatabaseProvider.getDatabaseProvider(dbConnection).getDriver();
        }
        DatabaseProvider.validateProvider(driverClass);
        properties.put(JDBC_DRIVER, driverClass);
    }

    private static void setURL(JSONObject datasource, Map<String, String> properties) {
        String url;
        if (datasource.containsKey(ConfigurationFileConstants.DATABASE_URL)) {
            url = (String) datasource.get(ConfigurationFileConstants.DATABASE_URL);
        } else {
            String dbConnection = (String) datasource.get(ConfigurationFileConstants.DATABASE_CONNECTION);
            url = DatabaseProvider.getDatabaseProvider(dbConnection).getUrl();
            url = replaceHost(datasource, url);
            url = replacePort(datasource, url);
            url = replaceDatabase(datasource, url);
        }
        properties.put(JDBC_URL, url);
    }

    private static void setUser(JSONObject datasource, Map<String, String> properties) {
        if (datasource.containsKey(ConfigurationFileConstants.DATABASE_USER)) {
            String user = (String) datasource.get(ConfigurationFileConstants.DATABASE_USER);
            properties.put(JDBC_USER, user);
        }
    }

    private static void setPassword(JSONObject datasource, Map<String, String> properties) {
        if (datasource.containsKey(ConfigurationFileConstants.DATABASE_PASSWORD)) {
            String password = (String) datasource.get(ConfigurationFileConstants.DATABASE_PASSWORD);
            properties.put(JDBC_PASSWORD, password);
        }
    }

    private static String replaceHost(JSONObject datasource, String url) {
        if (datasource.containsKey(ConfigurationFileConstants.DATABASE_HOST)) {
            String host = (String) datasource.get(ConfigurationFileConstants.DATABASE_HOST);
            return url.replace("?host", host);
        }
        return url;
    }

    private static String replacePort(JSONObject datasource, String url) {
        if (datasource.containsKey(ConfigurationFileConstants.DATABASE_PORT)) {
            String port = (String) datasource.get(ConfigurationFileConstants.DATABASE_PORT);
            return url.replace("?port", port);
        }
        return url;
    }

    private static String replaceDatabase(JSONObject datasource, String url) {
        if (datasource.containsKey(ConfigurationFileConstants.DATABASE_NAME)) {
            String database = (String) datasource.get(ConfigurationFileConstants.DATABASE_NAME);
            return url.replace("?database", database);
        }
        return url;
    }
}



