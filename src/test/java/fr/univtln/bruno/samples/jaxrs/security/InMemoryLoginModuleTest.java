package fr.univtln.bruno.samples.jaxrs.security;


import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.EnumSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryLoginModuleTest {

    private InMemoryLoginModule loginModule;

    @BeforeEach
    void setUp() {
        loginModule = new InMemoryLoginModule();
    }

    @Test
    void testAddUser() throws InvalidKeySpecException, NoSuchAlgorithmException {
        loginModule.addUser("Jane", "Doe", "jane.doe@nowhere.com", "password", EnumSet.of(InMemoryLoginModule.Role.USER));
        assertNotNull(loginModule.getUser("jane.doe@nowhere.com"));
    }

    @Test
    void testLogin() throws InvalidKeySpecException, NoSuchAlgorithmException {
        loginModule.addUser("Jane", "Doe", "jane.doe@nowhere.com", "password", EnumSet.of(InMemoryLoginModule.Role.USER));
        assertTrue(loginModule.login("jane.doe@nowhere.com", "password"));
        assertFalse(loginModule.login("jane.doe@nowhere.com", "wrongpassword"));
    }

    @Test
    void testRemoveUser() throws InvalidKeySpecException, NoSuchAlgorithmException {
        loginModule.addUser("Jane", "Doe", "jane.doe@nowhere.com", "password", EnumSet.of(InMemoryLoginModule.Role.USER));
        loginModule.removeUser("jane.doe@nowhere.com");
        assertNull(loginModule.getUser("jane.doe@nowhere.com"));
    }
}