
package com.library.library_api.controller;

import com.library.library_api.model.Book;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final List<Book> books = new ArrayList<>();

    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        books.add(book);
        return new ResponseEntity<>(book, HttpStatus.CREATED);
    }

    // Get all books
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(books);
    }

    // Get book by ID
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Integer id) {

        for (Book book : books) {
            if (book.getId().equals(id)) {
                return ResponseEntity.ok(book);
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
            @PathVariable Integer id,
            @RequestBody Book updatedBook) {

        for (Book book : books) {

            if (book.getId().equals(id)) {

                book.setTitle(updatedBook.getTitle());
                book.setAuthor(updatedBook.getAuthor());
                book.setCategory(updatedBook.getCategory());
                book.setAvailableCopies(updatedBook.getAvailableCopies());

                return ResponseEntity.ok(book);
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Integer id) {

        Iterator<Book> iterator = books.iterator();

        while (iterator.hasNext()) {

            Book book = iterator.next();

            if (book.getId().equals(id)) {
                iterator.remove();

                return ResponseEntity.ok(
                        "Book with ID " + id + " deleted successfully."
                );
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Book with ID " + id + " not found.");
    }
}
