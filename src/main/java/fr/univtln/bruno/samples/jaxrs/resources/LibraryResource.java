package fr.univtln.bruno.samples.jaxrs.resources;

import fr.univtln.bruno.samples.jaxrs.exceptions.BusinessException;
import fr.univtln.bruno.samples.jaxrs.exceptions.IllegalArgumentException;
import fr.univtln.bruno.samples.jaxrs.model.Library;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.java.Log;

import java.security.SecureRandom;
import java.util.Set;

@Log
@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
@Path("library")
public class LibraryResource {

    //A random number generator
    private static final SecureRandom random = new SecureRandom();

    @GET
    public Library getLibrary() {
        return Library.demoLibrary;
    }

    /**
     * The simpliest method that just return "hello" in plain text with GET on the default path "biblio".
     *
     * @return the string
     */
    @SuppressWarnings("SameReturnValue")
    @GET
    @Path("hello")
    @Produces(MediaType.TEXT_PLAIN)
    public String sayHello() {
        return "hello";
    }

    /**
     * An init method that add two authors with a PUT on the default path.
     *
     * @return the number of generated authors.
     * @throws IllegalArgumentException the illegal argument exception
     */
    @PUT
    @Path("init")
    public int init() throws BusinessException {
        Library.demoLibrary.removesAuthors();
        Library.Author author1 = Library.demoLibrary.addAuthor(Library.Author.builder().firstname("Alfred").name("Martin").build());
        Library.Author author2 = Library.demoLibrary.addAuthor(Library.Author.builder().firstname("Marie").name("Durand").build());

        Library.demoLibrary.addBook(Library.Book.builder().title("title1").authors(Set.of(author1)).build());
        Library.demoLibrary.addBook(Library.Book.builder().title("title2").authors(Set.of(author1, author2)).build());
        Library.demoLibrary.addBook(Library.Book.builder().title("title3").authors(Set.of(author2)).build());
        Library.demoLibrary.addBook(Library.Book.builder().title("title4").authors(Set.of(author2)).build());

        return Library.demoLibrary.getAuthorsNumber();
    }

    /**
     * An init method that add a given number of random authors whose names are just random letters on PUT.
     * The number of authors if given in the path avec bound to the name size. The needed format (an integer) is checked with a regular expression [0-9]+
     * The parameter is injected with @PathParam
     *
     * @param size the number of authors to add
     * @return the int number of generated authors.
     * @throws IllegalArgumentException the illegal argument exception
     */
    @PUT
    @Path("init/{size:[0-9]+}")
    public int init(@PathParam("size") int size) throws BusinessException {
        Library.demoLibrary.removesAuthors();
        for (int i = 0; i < size; i++)
            Library.demoLibrary.addAuthor(
                    Library.Author.builder()
                            .firstname(randomString(random.nextInt(6) + 2))
                            .name(randomString(random.nextInt(6) + 2)).build());
        return Library.demoLibrary.getAuthorsNumber();
    }

    /**
     * A random string generator
     *
     * @param targetStringLength the length of the String
     * @return a random String of the given length
     */
    private String randomString(int targetStringLength) {
        int letterA = 97;
        int letterZ = 122;
        return random.ints(letterA, letterZ + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

}
