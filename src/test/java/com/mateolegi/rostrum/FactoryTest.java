package com.mateolegi.rostrum;

import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static org.junit.jupiter.api.Assertions.*;

class FactoryTest {

    @Test
    void getEntityManager() {
        EntityManager manager = Factory.getEntityManager("rostrum");
        assertTrue(manager::isOpen);
    }

    @Test
    void getEntityManagerFactory() {
        EntityManagerFactory factory = Factory.getEntityManagerFactory("rostrum");
        assertNotNull(factory);
        assertTrue(factory::isOpen);
    }
}