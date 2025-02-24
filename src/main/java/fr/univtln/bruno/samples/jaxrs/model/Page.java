package fr.univtln.bruno.samples.jaxrs.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Generic pagination class that holds a list of items and pagination metadata.
 *
 * @param <T> the type of elements in the page
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
@ToString
public class Page<T> {
    @Min(1)
    long pageSize;
    
    @Min(0)
    long pageNumber;
    
    @Min(0)
    long elementTotal;
    
    @NotNull
    List<T> content;
    
    long pageTotal;
    
    private Page(long pageSize, long pageNumber, long elementTotal, List<T> content) {
        validateInput(pageSize, pageNumber, elementTotal, content);
        
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.elementTotal = elementTotal;
        this.content = Collections.unmodifiableList(content);
        this.pageTotal = calculatePageTotal(elementTotal, pageSize);
    }

    /**
     * Creates a new Page instance.
     *
     * @param pageSize     the size of each page
     * @param pageNumber   the current page number (0-based)
     * @param elementTotal the total number of elements
     * @param content      the content of the current page
     * @param <V>         the type of elements in the page
     * @return a new Page instance
     * @throws IllegalArgumentException if input parameters are invalid
     */
    public static <V> Page<V> newInstance(long pageSize, long pageNumber, long elementTotal, List<V> content) {
        return new Page<>(pageSize, pageNumber, elementTotal, content);
    }

    /**
     * Creates an empty page.
     *
     * @param <V> the type of elements in the page
     * @return an empty page
     */
    public static <V> Page<V> empty() {
        return new Page<>(1, 0, 0, Collections.emptyList());
    }

    /**
     * Checks if the current page is the first page.
     *
     * @return true if this is the first page
     */
    public boolean isFirst() {
        return pageNumber == 0;
    }

    /**
     * Checks if the current page is the last page.
     *
     * @return true if this is the last page
     */
    public boolean isLast() {
        return pageNumber == pageTotal - 1;
    }

    /**
     * Returns whether the page has content.
     *
     * @return true if the page has content
     */
    public boolean hasContent() {
        return !content.isEmpty();
    }

    /**
     * Gets the number of elements in the current page.
     *
     * @return the number of elements in the current page
     */
    public int getNumberOfElements() {
        return content.size();
    }

    private static long calculatePageTotal(long elementTotal, long pageSize) {
        return elementTotal == 0 ? 1 : (elementTotal + pageSize - 1) / pageSize;
    }

    private static void validateInput(long pageSize, long pageNumber, long elementTotal, List<?> content) {
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page number must not be negative");
        }
        if (elementTotal < 0) {
            throw new IllegalArgumentException("Total elements must not be negative");
        }
        Objects.requireNonNull(content, "Content must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Page<?> page)) return false;
        return pageSize == page.pageSize &&
                pageNumber == page.pageNumber &&
                elementTotal == page.elementTotal &&
                pageTotal == page.pageTotal &&
                content.equals(page.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageSize, pageNumber, elementTotal, content, pageTotal);
    }
}