package fr.univtln.bruno.samples.jaxrs.model;

import fr.univtln.bruno.samples.jaxrs.exceptions.BusinessException;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.*;

class LibraryModelTest {
    private static final Library modeleBibliotheque = Library.newInstance();
    private static final Library.Author AUTHOR_1 = Library.Author.builder().firstname("Jean").name("Martin").build();
    private static final Library.Author AUTHOR_2 = Library.Author.builder().firstname("Marie").name("Durand").build();

    /**
     * Adds two authors before each test.
     */
    @BeforeEach
    public void beforeEach() throws BusinessException {
        modeleBibliotheque.addAuthor(SerializationUtils.clone(AUTHOR_1));
        modeleBibliotheque.addAuthor(SerializationUtils.clone(AUTHOR_2));
    }

    @AfterEach
    public void afterEach() {
        modeleBibliotheque.removesAuthors();
    }

    @Test
    void addAuteur() throws BusinessException {
        Library.Author author = Library.Author.builder().firstname("John").name("Doe").biography("My life").build();
        modeleBibliotheque.addAuthor(SerializationUtils.clone(author));
        assertThat(author, samePropertyValuesAs(modeleBibliotheque.getAuthor(3), "id"));
    }

    @Test
    void addAuteurException() {
        Library.Author author = Library.Author.builder().id(1).firstname("John").name("Doe").build();
        assertThrows(BusinessException.class, () -> modeleBibliotheque.addAuthor(SerializationUtils.clone(author)));
    }

    @Test
    void updateAuteur() throws BusinessException {
        Library.Author author = Library.Author.builder().firstname("John").name("Doe").build();
        modeleBibliotheque.updateAuteur(1, SerializationUtils.clone(author));
        assertThat(author, samePropertyValuesAs(modeleBibliotheque.getAuthor(1), "id"));
    }

    @Test
    void removeAuteur() throws BusinessException {
        modeleBibliotheque.removeAuthor(1);
        assertEquals(1, modeleBibliotheque.getAuthorsNumber());
        assertEquals(2, modeleBibliotheque.getAuthors().values().iterator().next().getId());
    }

    @Test
    void getAuteur() throws BusinessException {
        Library.Author author = modeleBibliotheque.getAuthor(1);
        assertThat(author, samePropertyValuesAs(AUTHOR_1, "id"));
    }

    @Test
    void getAuteurSize() {
        assertEquals(2, modeleBibliotheque.getAuthorsNumber());
    }

    @Test
    void supprimerAuteurs() {
        modeleBibliotheque.removesAuthors();
        assertEquals(0, modeleBibliotheque.getAuthorsNumber());
        assertEquals(0, modeleBibliotheque.getAuthors().size());
    }

    @Test
    void of() {
        Library modeleBibliotheque1 = Library.newInstance();
        assertNotNull(modeleBibliotheque1);
    }

    @Test
    void getAuteurs() {
        assertNotNull(modeleBibliotheque.getAuthors());
    }
}