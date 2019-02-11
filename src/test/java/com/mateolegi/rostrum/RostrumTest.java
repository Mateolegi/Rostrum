package com.mateolegi.rostrum;

import com.mateolegi.rostrum.entities.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RostrumTest {

    @Test
    void findAll() {
        List<User> userList = Rostrum.findAll(User.class);
        assertNotNull(userList);
    }

    @Test
    void find() {
        User u = Rostrum.find(User.class, 1L);
        assertNotNull(u);
    }

    @Test
    void save() {
        User u = new User();
        u.setUsername("Test");
        u.setPassword("password");
        u.setName("Prueba");
        u.setLastName("Prueba");
        u.setActive(true);
        Factory.getEntityManager().persist(u);
        //User uSaved = Rostrum.save(u);
        //System.out.println(uSaved.getId());
    }

    @Test
    void update() {
    }

    @Test
    void updateIfExists() {
    }

    @Test
    void delete() {
    }

    @Test
    void exists() {
    }
}