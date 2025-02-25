package fr.univtln.bruno.samples.jaxrs.client;

import org.glassfish.jersey.jackson.JacksonFeature;

import fr.univtln.bruno.samples.jaxrs.model.Library.Author;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class SimpleClient {

    public static void main(String[] args) {
        String token = "XXX-YYY-ZZZ";
        String baseUrl = "http://localhost:9998/mylibrary";

        try (Client client = ClientBuilder.newBuilder()
                .register(JacksonFeature.class)
                .build()) {

            WebTarget baseTarget = client.target(baseUrl);

            // Initialize library
            try {
                String initResponse = baseTarget.path("library/init")
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.entity("", MediaType.TEXT_PLAIN), String.class);
                System.out.println("Library initialized: " + initResponse);
            } catch (WebApplicationException e) {
                System.err.println("Failed to initialize library: " + e.getMessage());
                return;
            }

            // Get author
            WebTarget authorTarget = baseTarget.path("authors/{id}");
            try (Response response = authorTarget
                    .resolveTemplate("id", "1")
                    .request(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .get()) {

                if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                    Author author = response.readEntity(Author.class);
                    System.out.println("Author: " + author);
                } else {
                    String error = response.hasEntity() ? response.readEntity(String.class) : "Unknown error";
                    System.err.printf("Error %d: %s%n", response.getStatus(), error);
                }
            }
        } catch (ProcessingException e) {
            System.err.println("Communication error: " + e.getMessage());
        }
    }
}