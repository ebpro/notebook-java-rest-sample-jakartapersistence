package fr.univtln.bruno.samples.jaxrs.model;

import com.fasterxml.jackson.annotation.*;
import fr.univtln.bruno.samples.jaxrs.exceptions.BusinessException;
import fr.univtln.bruno.samples.jaxrs.exceptions.NotFoundException;
import fr.univtln.bruno.samples.jaxrs.resources.PaginationInfo;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.annotation.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.impl.factory.primitive.LongObjectMaps;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.univtln.bruno.samples.jaxrs.model.Library.Field.valueOf;


/**
 * The type Biblio model. An in memory instance of a Library model. Kind of a mock.
 */
@Log
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor(staticName = "newInstance")
@XmlRootElement
public class Library {
    //An in memory instance of a Library model. Kind of a mock.
    public static final Library demoLibrary = Library.newInstance();

    private static final AtomicLong lastAuthorId = new AtomicLong(0);
    private static final AtomicLong lastBookId = new AtomicLong(0);

    @JsonIgnore
    final MutableLongObjectMap<Author> authors = LongObjectMaps.mutable.empty();
    final MutableLongObjectMap<Book> books = LongObjectMaps.mutable.empty();

    private static final String AUTHOR_NOT_FOUND = "Author not found";
    /**
     * used mainly to provide easy XML Serialization
     *
     * @return the list of authors
     */
    @XmlElementWrapper(name = "authors")
    @XmlElements({@XmlElement(name = "author")})
    @JsonProperty("authors")
    public List<Author> getAuthorsAsList() {
        return authors.toList();
    }

    /**
     * used mainly to provide easy XML Serialization
     *
     * @return the list of books
     */
    @XmlElementWrapper(name = "books")
    @XmlElements({@XmlElement(name = "book")})
    @JsonProperty("books")
    public List<Book> getBooksAsList() {
        return books.toList();
    }

    /**
     * Adds an author to the model.
     *
     * @param author the author without an iÒd.
     * @return the author with its id.
     * @throws BusinessException if the format is incorrect.
     */
    public Author addAuthor(Author author) throws BusinessException {
        if (author.id != 0) throw new BusinessException(Response.Status.NOT_ACCEPTABLE, "Id shouldn't be given");

        author.id = lastAuthorId.incrementAndGet();
        authors.put(author.id, author);
        return author;
    }

    /**
     * Adds a book to the model
     *
     * @param book the book without its id and a non-empty authors set.
     * @return the book with its id.
     * @throws BusinessException if the format is incorrect.
     */
    public Book addBook(Book book) throws BusinessException {
        if (book.id != 0) throw new BusinessException(Response.Status.INTERNAL_SERVER_ERROR, "Id shouldn't be given");
        if (book.authors == null || book.authors.isEmpty())
            throw new BusinessException(Response.Status.INTERNAL_SERVER_ERROR, "Author set is mandatory");
        book.id = lastBookId.incrementAndGet();
        book.authors.stream().forEach(auteur -> {
            if (auteur.books == null) auteur.books = new HashSet<>();
            auteur.books.add(book);
        });
        books.put(book.id, book);
        return book;
    }

    /**
     * Updates auteur by id with data contained in an author instance (without id).
     *
     * @param id     the id of the author to update
     * @param author the author instance containing the data without id.
     * @return the update author
     * @throws BusinessException if the author if not found or the data invalid.
     */
    public Author updateAuteur(long id, Author author) throws BusinessException {
        if (author.id != 0)
            throw new BusinessException(Response.Status.NOT_ACCEPTABLE, "Id shouldn't be given in data");
        author.id = id;
        if (!authors.containsKey(id)) throw new BusinessException(Response.Status.NOT_FOUND, AUTHOR_NOT_FOUND);
        authors.put(id, author);
        return author;
    }

    /**
     * Removes one auteur by id.
     *
     * @param id the id
     * @throws BusinessException if not found
     */
    public void removeAuthor(long id) throws BusinessException {
        if (!authors.containsKey(id)) throw new BusinessException(Response.Status.NOT_FOUND, AUTHOR_NOT_FOUND);
        authors.remove(id);
    }

    /**
     * Gets one auteur id.
     *
     * @param id the id
     * @return the author
     * @throws NotFoundException if not found exception
     */
    public Author getAuthor(long id) throws BusinessException {
        if (!authors.containsKey(id)) throw new BusinessException(Response.Status.NOT_FOUND, AUTHOR_NOT_FOUND);
        return authors.get(id);
    }

    /**
     * Gets the number of authors.
     *
     * @return the number of authors
     */
    @JsonIgnore
    public int getAuthorsNumber() {
        return authors.size();
    }

    private Stream<Author> buildSortedFilteredStream(PaginationInfo paginationInfo) {
        //We build a author stream, first we add sorting
        Stream<Author> authorStream = authors.stream()
                .sorted(Comparator.comparing(auteur -> switch (valueOf(paginationInfo.getSortKey().toUpperCase())) {
                    case NAME -> auteur.getName();
                    case FIRSTNAME -> auteur.getFirstname();
                    default -> throw new InvalidParameterException();
                }));

        //The add filters according to parameters
        if (paginationInfo.getName() != null)
            authorStream = authorStream.filter(author -> author.getName().equalsIgnoreCase(paginationInfo.getName()));
        if (paginationInfo.getFirstname() != null)
            authorStream = authorStream.filter(author -> author.getFirstname().equalsIgnoreCase(paginationInfo.getFirstname()));
        if (paginationInfo.getBiography() != null)
            authorStream = authorStream.filter(author -> author.getBiography().contains(paginationInfo.getBiography()));

        return authorStream;
    }

    /**
     * Returns a sorted, filtered and paginated list of authors.
     *
     * @param paginationInfo the pagination info
     * @return the sorted, filtered page.
     */
    public Page<Author> getAuthorsWithFilter(PaginationInfo paginationInfo) {


        //We count the total number of results before limit and offset
        long elementTotal = buildSortedFilteredStream(paginationInfo).count();

        Stream<Author> authorStream = buildSortedFilteredStream(paginationInfo);
        //Finally add pagination instructions.
        if ((paginationInfo.getPage() > 0) && (paginationInfo.getPageSize() > 0)) {
            authorStream = authorStream
                    .skip(paginationInfo.getPageSize() * (paginationInfo.getPage() - 1))
                    .limit(paginationInfo.getPageSize());
        }

        return Page.newInstance(paginationInfo.getPageSize(),
                paginationInfo.getPage(),
                elementTotal,
                authorStream.collect(Collectors.toList())
        );
    }

    /**
     * Removes all authors.
     */
    public void removesAuthors() {
        authors.clear();
        books.clear();
        lastAuthorId.set(0);
        lastBookId.set(0);
    }

    /**
     * The list of fields of author that can used in filters.
     */
    public enum Field {
        NAME,
        FIRSTNAME,
        BIOGRAPHY
    }

    @Builder
    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor
    @AllArgsConstructor

    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)

    //To enable XML Serialization
    @XmlRootElement
    //Because getter are generated by lombok
    @XmlAccessorType(XmlAccessType.FIELD)

    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @ToString(exclude = "authors")
    public static class Book implements Serializable {
        @EqualsAndHashCode.Include
        //@XmlTransient
        long id;
        String title;
        @XmlIDREF
        @XmlElementWrapper(name = "authors")
        @XmlElements({@XmlElement(name = "author")})
        @JsonIdentityReference(alwaysAsId = true)
        private Set<Author> authors;

        @JsonIgnore
        @XmlID
        @XmlAttribute(name = "id")
        private String getXmlID() {
            return String.format("%s-%s", this.getClass().getSimpleName(), this.getId());
        }

    }

    /**
     * The type Author.
     */
    @Builder
    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor
    @AllArgsConstructor

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")

    //To enable XML Serialization
    @XmlRootElement
    //Because getter are generated by lombok
    @XmlAccessorType(XmlAccessType.FIELD)
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @ToString(exclude = "books")
    public static class Author implements Serializable {
        @EqualsAndHashCode.Include
        //@XmlTransient
        long id;
        String name;
        String firstname;
        String biography;
        @XmlIDREF
        @XmlElementWrapper(name = "books")
        @XmlElements({@XmlElement(name = "book")})
        @JsonIdentityReference(alwaysAsId = true)
        private Set<Book> books;

        @JsonIgnore
        @XmlID
        @XmlAttribute(name = "id")
        private String getXmlID() {
            return String.format("%s-%s", this.getClass().getSimpleName(), this.getId());
        }
    }
}
