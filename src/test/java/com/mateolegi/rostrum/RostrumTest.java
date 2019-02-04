package com.mateolegi.rostrum;

import com.mateolegi.rostrum.entities.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RostrumTest {

    @Test
    void findAll() {
        List<User> userList = new User().findAll();
        assertFalse(userList.isEmpty());
    }

    @Test
    void find() {

    }

    @Test
    void save() {
        User u = new User();
        u.setUsername("Mateolegi2");
        u.setPassword("password");
        u.setName("Mateo");
        u.setLastName("Leal");
        u.setActive(true);
        User uSaved = u.save();
        System.out.println(uSaved.getId());
        System.out.println(uSaved.getPassword());
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