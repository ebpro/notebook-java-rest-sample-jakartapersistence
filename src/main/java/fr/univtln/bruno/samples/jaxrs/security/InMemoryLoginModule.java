package fr.univtln.bruno.samples.jaxrs.security;

import io.jsonwebtoken.Jwts;
import lombok.AccessLevel;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

/**
 * this class model a simple in memory-role-based authentication database (RBAC).
 * Passwords are salted and hashed.
 */
@Log
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InMemoryLoginModule {
    /**
     * The constant USER_DATABASE mocks a user database in memory.
     */
    public static final InMemoryLoginModule USER_DATABASE = new InMemoryLoginModule();

    /**
     * The constant KEY is used as a signing key for the bearer JWT token.
     * It is used to check that the token hasn't been modified.
     */
    public static final SecretKey KEY = Jwts.SIG.HS256.key().build();

    //We add three demo users.
    static {
        try {
            USER_DATABASE.addUser("John", "Doe", "john.doe@nowhere.com", "admin", EnumSet.of(Role.ADMIN));
            USER_DATABASE.addUser("William", "Smith", "william.smith@here.net", "user", EnumSet.of(Role.USER));
            USER_DATABASE.addUser("Mary", "Robert", "mary.roberts@here.net", "user", EnumSet.of(Role.USER));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            log.severe("In memory user database error "+e.getLocalizedMessage());
        }
    }

    final Map<String, User> users = new HashMap<>();

    public static boolean isInRoles(Set<Role> rolesSet, String username) {
        return !(Collections.disjoint(rolesSet, InMemoryLoginModule.USER_DATABASE.getUserRoles(username)));
    }

    /**
     * Add user.
     *
     * @param firstname the firstname
     * @param lastname  the lastname
     * @param email     the email
     * @param password  the password
     * @param roles     the roles
     * @throws InvalidKeySpecException  the invalid key spec exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public void addUser(String firstname, String lastname, String email, String password, Set<Role> roles)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        users.put(email, User.builder().firstName(firstname).lastName(lastname).email(email).password(password).roles(roles).build());
    }

    /**
     * Gets users.
     *
     * @return the users
     */
    public Map<String, User> getUsers() {
        return Collections.unmodifiableMap(users);
    }

    /**
     * Remove user.
     *
     * @param email the email
     */
    public void removeUser(String email) {
        users.remove(email);
    }

    /**
     * Check password boolean.
     *
     * @param email    the email
     * @param password the password
     * @return the boolean
     */
    public boolean login(String email, String password) {
        return users.get(email).checkPassword(password);
    }

    /**
     * Gets user.
     *
     * @param email the email
     * @return the user
     */
    public User getUser(String email) {
        return users.get(email);
    }

    /**
     * Gets user roles.
     *
     * @param email the email
     * @return the user roles
     */
    public Set<Role> getUserRoles(String email) {
        User user = users.get(email);
        return user==null?EnumSet.of(Role.GUEST):user.getRoles();
    }

    @SuppressWarnings("SameReturnValue")
    public boolean logout() {
        return false;
    }

    /**
     * The enum Role.
     */
    public enum Role {
        /**
         * Admin role.
         */
        ADMIN,
        /**
         * User role.
         */
        USER,
        /**
         * Guest role.
         */
        GUEST
    }
}
