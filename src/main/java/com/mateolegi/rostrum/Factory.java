package com.mateolegi.rostrum;

import com.mateolegi.rostrum.constant.PropertiesConstants;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Responsible for managing the connections to the persistence context.
 * @author <a href="mateolegi.github.io">Mateo Leal</a>
 * @version 1.0.0
 */
public class Factory {

    private static final EntityManagerFactory FACTORY;
    private static EntityManager manager = null;

    static {
        FACTORY = Persistence.createEntityManagerFactory(Properties
                .getString(PropertiesConstants.JPA_PERSISTENCE_UNIT));
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
}
