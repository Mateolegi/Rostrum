package com.mateolegi.rostrum;

import com.mateolegi.rostrum.entities.User;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RostrumTest {

    @Test
    void findAll() {
        User user = new User();
        List<User> users = (List<User>) user.findAll();
        System.out.println(users.size());
        users.forEach(System.out::println);
        assertNotNull(users);
    }

    @Test
    void find() {
        User user = (User) new User().find(1L);
        assertNotNull(user);
        assertEquals(1L, user.getId());
    }

    @Test
    @Order(1)
    void save() {
        User u = new User();
        u.setUsername("Test");
        u.setPassword("password");
        u.setName("Prueba");
        u.setLastName("Prueba");
        u.setActive(1);
        User uSaved = (User) u.save();
        assertNotNull(uSaved);
        assertTrue(BCrypt.verifyHash("password", uSaved.getPassword()));
        assertTrue(Rostrum.exists(uSaved));
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
        User user = (User) new User().find(1L);
        assertNotNull(user);
        assertTrue(user::exists);
        User notExistingUser = new User();
        notExistingUser.setId(Long.MAX_VALUE);
        assertFalse(notExistingUser::exists);
    }
}