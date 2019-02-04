package com.mateolegi.rostrum;

import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

class FactoryTest {

    @Test
    void getEntityManager() {
        EntityManager manager = Factory.getEntityManager();
        assertTrue(manager.isOpen());
    }

    @Test
    void closeFactory() {
        Factory.closeFactory();
        assertThrows(IllegalStateException.class, Factory::getEntityManager);
    }
}