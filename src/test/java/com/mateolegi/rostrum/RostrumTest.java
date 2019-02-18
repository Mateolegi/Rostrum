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
        User u = User.find(2L);
        System.out.println(u.getPassword());
        System.out.println(BCrypt.verifyHash("password", u.getPassword()));
        assertNotNull(u);
    }

    @Test
    void save() {
        User u = new User();
        u.setUsername("Test");
        u.setPassword("password");
        u.setName("Prueba");
        u.setLastName("Prueba");
        u.setActive(1);
        User uSaved = Rostrum.save(u);
        System.out.println(uSaved.getId());
    }

    @Test
    void update() {
        User u = Rostrum.find(User.class, 2L);
        u.setName("Test");
        User uUpdated = Rostrum.update(u);
        assertEquals(u.getName(), uUpdated.getName());
        assertNotNull(uUpdated.getUpdatedAt());
    }

    @Test
    void updateIfExists() {
    }

    @Test
    void delete() {
    }

    @Test
    void exists() {
        assertTrue(Rostrum.exists(Rostrum.find(User.class, 1L)));
        User notExistingUser = new User();
        notExistingUser.setId(Long.MAX_VALUE);
        assertFalse(Rostrum.exists(notExistingUser));
    }
}