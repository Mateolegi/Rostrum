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

    static {
        FACTORY = Persistence.createEntityManagerFactory(Properties
                .getString(PropertiesConstants.JPA_PERSISTENCE_UNIT));
    }

    /**
     * Generates EntityManager to interact with the persistence context.
     * @return generated EntityManager
     */
    public static EntityManager getEntityManager() {
        return FACTORY.createEntityManager();
    }

    /**
     * Closes EntityManagerFactory.
     */
    public static void closeFactory() {
        FACTORY.close();
    }
}
