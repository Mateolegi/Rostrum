package com.mateolegi.rostrum;

import com.mateolegi.rostrum.entities.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Main {

    private static final EntityManagerFactory factory = Persistence
            .createEntityManagerFactory("rostrum");

    private static EntityManager getEntityManager() {
        return factory.createEntityManager();
    }

    private static void createUser() {
        User u = new User();
        u.setUsername("Mateolegi");
        u.setPassword("test");
        u.setName("Mateo");
        u.setLastName("Leal");
        u.setActive(true);
    }

    public static void main(String[] args) {
        //User u = new User(manager);
        //u.save();
    }
}
