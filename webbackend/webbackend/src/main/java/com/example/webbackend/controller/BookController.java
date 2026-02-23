package com.example.webbackend.controller;

import com.example.webbackend.entity.Book;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookController {

    private List<Book> books = new ArrayList<>();
    private Long nextId = 1L;

    public BookController() {
        // Add 15 books with varied data for testing
        books.add(new Book(nextId++, "Spring Boot in Action", "Craig Walls", 39.99));
        books.add(new Book(nextId++, "Effective Java", "Joshua Bloch", 45.00));
        books.add(new Book(nextId++, "Clean Code", "Robert Martin", 42.50));
        books.add(new Book(nextId++, "Java Concurrency in Practice", "Brian Goetz", 49.99));
        books.add(new Book(nextId++, "Design Patterns", "Gang of Four", 54.99));
        books.add(new Book(nextId++, "Head First Java", "Kathy Sierra", 35.00));
        books.add(new Book(nextId++, "Spring in Action", "Craig Walls", 44.99));
        books.add(new Book(nextId++, "Clean Architecture", "Robert Martin", 39.99));
        books.add(new Book(nextId++, "Refactoring", "Martin Fowler", 47.50));
        books.add(new Book(nextId++, "The Pragmatic Programmer", "Andrew Hunt", 41.99));
        books.add(new Book(nextId++, "You Don't Know JS", "Kyle Simpson", 29.99));
        books.add(new Book(nextId++, "JavaScript: The Good Parts", "Douglas Crockford", 32.50));
        books.add(new Book(nextId++, "Eloquent JavaScript", "Marijn Haverbeke", 27.99));
        books.add(new Book(nextId++, "Python Crash Course", "Eric Matthes", 38.00));
        books.add(new Book(nextId++, "Automate the Boring Stuff", "Al Sweigart", 33.50));
    }

    // GET /api/books (all books)
    @GetMapping("/books")
    public List<Book> getBooks() {
        return books;
    }

    // GET /api/books/{id} (book by ID)
    @GetMapping("/books/{id}")
    public Book getBook(@PathVariable Long id) {
        return books.stream().filter(book -> book.getId().equals(id))
                .findFirst().orElse(null);
    }

    // POST /api/books (create book)
    @PostMapping("/books")
    public List<Book> createBook(@RequestBody Book book) {
        books.add(book);
        return books;
    }

    // Search by title
    @GetMapping("/books/search")
    public List<Book> searchByTitle(
            @RequestParam(required = false, defaultValue = "") String title
    ) {
        if(title.isEmpty()) {
            return books;
        }

        return books.stream()
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }

    // Price range
    @GetMapping("/books/price-range")
    public List<Book> getBooksByPrice(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        return books.stream()
                .filter(book -> {
                    boolean min = minPrice == null || book.getPrice() >= minPrice;
                    boolean max = maxPrice == null || book.getPrice() <= maxPrice;

                    return min && max;
                }).collect(Collectors.toList());
    }

    // Sort
    @GetMapping("/books/sorted")
    public List<Book> getSortedBooks(
            @RequestParam(required = false, defaultValue = "title") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order
    ){
        Comparator<Book> comparator;

        switch(sortBy.toLowerCase()) {
            case "author":
                comparator = Comparator.comparing(Book::getAuthor);
                break;
            case "title":
                comparator = Comparator.comparing(Book::getTitle);
            default:
                comparator = Comparator.comparing(Book::getTitle);
                break;
        }

        if("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        return books.stream().sorted(comparator)
                .collect(Collectors.toList());
    }

    // Start Homework 1
    // PUT endpoint (update book)
    @PutMapping("/books/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book bookToBeUpdated) {
        return books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .map(currentBookInArray -> {
                    bookToBeUpdated.setId(id);
                    int indexOfCurrentBookInArray = books.indexOf(currentBookInArray);
                    books.set(indexOfCurrentBookInArray, bookToBeUpdated);
                    return bookToBeUpdated;
                })
                .orElse(null);
    }

    // PATCH endpoint (partial update)
    @PatchMapping("/books/{id}")
    public Book partiallyUpdateBook(@PathVariable Long id, @RequestBody Book bookToBePartiallyUpdated) {
        return books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .map(currentBookInArray -> {
                    Optional.ofNullable(bookToBePartiallyUpdated.getTitle())
                            .ifPresent(currentBookInArray::setTitle);

                    Optional.ofNullable(bookToBePartiallyUpdated.getAuthor())
                            .ifPresent(currentBookInArray::setAuthor);

                    Optional.ofNullable(bookToBePartiallyUpdated.getPrice())
                            .ifPresent(currentBookInArray::setPrice);

                    return currentBookInArray;
                })
                .orElse(null);
    }

    // DELETE endpoint (remove book)
    @DeleteMapping("/books/{id}")
    public String deleteBook(@PathVariable Long id) {
        return books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .map(bookToBeDeletedInArray -> {
                    books.remove(bookToBeDeletedInArray);
                    return "The book is deleted.";
                })
                .orElse("The book is not found");
    }

    // GET endpoint with pagination
    @GetMapping("/books/paged")
    public List<Book> getBooksWithPagination(
            @RequestParam(defaultValue = "0") int indexOfPage,
            @RequestParam(defaultValue = "5") int itemsPerPage
    ) {
        int numberOfBooksToSkip = indexOfPage * itemsPerPage;

        return books.stream()
                .skip(numberOfBooksToSkip)
                .limit(itemsPerPage)
                .collect(Collectors.toList());
    }

    // Advanced GET endpoint with filtering, sorting, and pagination combined in the valid order
    @GetMapping("/books/advanced")
    public List<Book> advancedQuery(
            @RequestParam(required = false) String titleFilter,
            @RequestParam(required = false) String authorFilter,
            @RequestParam(required = false) Double minimumPrice,
            @RequestParam(required = false) Double maximumPrice,
            @RequestParam(defaultValue = "title") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "0") int indexOfPage,
            @RequestParam(defaultValue = "5") int itemsPerPage
    ) {

        Comparator<Book> bookComparator;

        switch (sortField.toLowerCase()) {
            case "author":
                bookComparator = Comparator.comparing(Book::getAuthor);
                break;
            case "price":
                bookComparator = Comparator.comparing(Book::getPrice);
                break;
            case "title":
                bookComparator = Comparator.comparing(Book::getTitle);
                break;
            default:
                bookComparator = Comparator.comparing(Book::getTitle);
                break;
        }

        if ("desc".equalsIgnoreCase(sortDirection)) {
            bookComparator = bookComparator.reversed();
        }

        int numberOfBooksToSkip = indexOfPage * itemsPerPage;

        return books.stream()
                .filter(book -> {
                    boolean matchesTitle =
                            titleFilter == null ||
                            book.getTitle().toLowerCase().contains(titleFilter.toLowerCase());

                    boolean matchesAuthor =
                            authorFilter == null ||
                            book.getAuthor().toLowerCase().contains(authorFilter.toLowerCase());

                    boolean matchesMinimumPrice =
                            minimumPrice == null ||
                            book.getPrice() >= minimumPrice;

                    boolean matchesMaximumPrice =
                            maximumPrice == null ||
                            book.getPrice() <= maximumPrice;

                    return matchesTitle && matchesAuthor && matchesMinimumPrice && matchesMaximumPrice;
                })

                .sorted(bookComparator)

                .skip(numberOfBooksToSkip)
                .limit(itemsPerPage)

                .collect(Collectors.toList());
    }
}