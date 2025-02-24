package fr.univtln.bruno.samples.jaxrs.client;

import fr.univtln.bruno.samples.jaxrs.model.Library;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.java.Log;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * REST Client for the Library API.
 * Demonstrates various HTTP operations and authentication methods.
 */
@Log
public class BiblioClient {
    private static final String BASE_URL = "http://localhost:9998/mylibrary";
    private static final String AUTH_EMAIL = "john.doe@nowhere.com";
    private static final String AUTH_PASSWORD = "admin";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String BASIC_PREFIX = "Basic ";

    /**
     * Main method demonstrating the REST client functionality.
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget webResource = client.target(BASE_URL);
            
            try {
                initializeLibrary(webResource);
                fetchAuthors(webResource);
                fetchAuthorById(webResource, 1);
                
                Optional<String> token = authenticate(webResource);
                token.ifPresent(t -> accessSecuredEndpoint(webResource, t));
                
            } catch (WebApplicationException e) {
                log.severe(String.format("REST API error: Status=%d, Message=%s", 
                    e.getResponse().getStatus(), 
                    e.getMessage()));
            } catch (Exception e) {
                log.severe("Unexpected error: " + e.getMessage());
            }
        }
    }

    /**
     * Initializes the library with default data.
     * @param webResource the web target for the API
     * @return initialization response message
     */
    private static String initializeLibrary(WebTarget webResource) {
        String response = webResource.path("library/init")
                .request()
                .put(Entity.entity("", MediaType.TEXT_PLAIN), String.class);
        log.info("Library initialized: " + response);
        return response;
    }

    /**
     * Fetches all authors from the library.
     * @param webResource the web target for the API
     * @return JSON string containing author data
     */
    private static String fetchAuthors(WebTarget webResource) {
        String response = webResource.path("authors")
                .request()
                .get(String.class);
        log.info("Authors: " + response);
        return response;
    }

    /**
     * Fetches a specific author by ID.
     * @param webResource the web target for the API
     * @param id the author ID
     * @return Author object
     */
    private static Library.Author fetchAuthorById(WebTarget webResource, int id) {
        Library.Author author = webResource.path("authors/" + id)
                .request()
                .get(Library.Author.class);
        log.info("Author details: " + author);
        return author;
    }

    /**
     * Authenticates with the API using Basic authentication.
     * @param webResource the web target for the API
     * @return Optional containing the JWT token if authentication successful
     */
    private static Optional<String> authenticate(WebTarget webResource) {
        try {
            String credentials = AUTH_EMAIL + ":" + AUTH_PASSWORD;
            String encodedCredentials = java.util.Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            
            String token = webResource.path("setup/login")
                    .request()
                    .accept(MediaType.TEXT_PLAIN)
                    .header("Authorization", BASIC_PREFIX + encodedCredentials)
                    .get(String.class);

            if (!token.isBlank()) {
                log.info("Authentication successful - token received");
                return Optional.of(token);
            }
        } catch (WebApplicationException e) {
            log.warning("Authentication failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Accesses a secured endpoint using JWT authentication.
     * @param webResource the web target for the API
     * @param token the JWT token
     * @return response from the secured endpoint
     */
    private static String accessSecuredEndpoint(WebTarget webResource, String token) {
        String result = webResource.path("setup/secured")
                .request()
                .header("Authorization", BEARER_PREFIX + token)
                .get(String.class);
        log.info("Secured endpoint response: " + result);
        return result;
    }
}