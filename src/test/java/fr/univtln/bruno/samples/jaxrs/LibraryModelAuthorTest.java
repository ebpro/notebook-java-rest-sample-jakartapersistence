package fr.univtln.bruno.samples.jaxrs;

import fr.univtln.bruno.samples.jaxrs.model.Library;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LibraryModelAuthorTest {

    @Test
    public void AuteurCreationTest() {
        long id = 1;
        String nom = "Doe", prenom = "John", bio = "My life";
        Library.Author author = Library.Author.builder()
                .id(id)
                .name(nom)
                .firstname(prenom)
                .biography(bio)
                .build();
        assertThat(author, allOf(hasProperty("name", is(nom)),
                hasProperty("firstname", is(prenom)),
                hasProperty("biography", is(bio))));
    }

}
