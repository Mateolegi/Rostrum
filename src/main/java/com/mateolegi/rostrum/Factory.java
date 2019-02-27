package com.mateolegi.rostrum;

import com.mateolegi.rostrum.constant.PropertiesConstants;
import com.mateolegi.rostrum.exception.PropertyNotFoundException;
import org.eclipse.persistence.jpa.PersistenceProvider;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Responsible for managing the connections to the persistence context.
 * @author <a href="mateolegi.github.io">Mateo Leal</a>
 * @version 1.0.0
 */
public class Factory {

    private static final EntityManagerFactory FACTORY;
    private static EntityManager manager = null;

    static {
        String url = getProperty(PropertiesConstants.JPA_JDBC_URL);
        String persistenceUnit = getProperty(PropertiesConstants.JPA_PERSISTENCE_UNIT);
        Map<String, String> properties = getDatabaseProperties();
//        if (Objects.isNull(url)) {
//            FACTORY = Persistence.createEntityManagerFactory(persistenceUnit);
//        } else {
            FACTORY = new PersistenceProvider()
                    .createContainerEntityManagerFactory(new RostrumPersistenceUnitInfo("rostrum"), getDatabaseProperties());
//                    .createEntityManagerFactory(persistenceUnit, getDatabaseProperties());
//        }
        System.out.println("pasa");
        if (FACTORY.getProperties().isEmpty()) System.out.println("no hay propiedades");
        FACTORY.getProperties().forEach((key, val) -> System.out.println("key: " + key + ", value: " + val));
    }

    /**
     * Generates EntityManager to interact with the persistence context.
     * @return generated EntityManager
     */
    public static synchronized EntityManager getEntityManager() {
        if (manager == null || !manager.isOpen()) {
            manager = FACTORY.createEntityManager();
        }
        return manager;
    }

    /**
     * Closes EntityManagerFactory.
     */
    public static void closeFactory() {
        FACTORY.close();
    }

    private static Map<String, String> getDatabaseProperties() {
        Map<String, String> properties = new HashMap<>();
        Arrays.stream(new String[] {
                PropertiesConstants.JPA_JDBC_URL,
                PropertiesConstants.JPA_JDBC_DRIVER,
                PropertiesConstants.JPA_JDBC_USER,
                PropertiesConstants.JPA_JDBC_PASSWORD
        }).forEach(key -> {
            String prop = getProperty(key);
            if (Objects.nonNull(prop)) {
                String keyName = key.replace("jpa", "javax.persistence");
                properties.put(keyName, prop);
            }
        });
        return properties;
    }

    private static String getProperty(String key) {
        try {
            return Properties.getString(key);
        } catch (PropertyNotFoundException e) {
            return null;
        }
    }
}
