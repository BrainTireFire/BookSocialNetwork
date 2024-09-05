package com.tengo.book.book;

import com.tengo.book.common.PagedResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("books")
@RequiredArgsConstructor
@Tag(name = "Book", description = "Book API")
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<Integer> saveBook(
            @RequestBody @Valid BookRequest bookRequest,
            Authentication connectUser
    ) {
        return ResponseEntity.ok(bookService.saveBook(bookRequest, connectUser));
    }

    @GetMapping("{book-id}")
    public ResponseEntity<BookResponse> findBookById(
            @PathVariable("book-id") Integer bookId
    ) {
        return ResponseEntity.ok(bookService.findById(bookId));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<BookResponse>> findAllBooks(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectUser
    ) {
        return ResponseEntity.ok(bookService.findAllBooks(page, size, connectUser));
    }

    @GetMapping("/owner")
    public ResponseEntity<PagedResponse<BookResponse>> findAllBooksByOwner(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectUser
    ) {
        return ResponseEntity.ok(bookService.findAllBooksByOwner(page, size, connectUser));
    }

    @GetMapping("/borrowed")
    public ResponseEntity<PagedResponse<BorrowedBookResponse>> findAllBorrowedBooks(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectUser
    ) {
        return ResponseEntity.ok(bookService.findAllBorrowedBooks(page, size, connectUser));
    }

    @GetMapping("/returned")
    public ResponseEntity<PagedResponse<BorrowedBookResponse>> findAllReturnedBooks(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectUser
    ) {
        return ResponseEntity.ok(bookService.findAllReturnedBooks(page, size, connectUser));
    }

    @PatchMapping("/shareable/{book-id}")
    public ResponseEntity<Integer> updateShareableStatus(
        @PathVariable("book-id") Integer bookId,
        Authentication connectUser
    ) {
        return ResponseEntity.ok(bookService.updateShareableStatus(bookId, connectUser));
    }

    @PatchMapping("/archived/{book-id}")
    public ResponseEntity<Integer> updateArchivedStatus(
            @PathVariable("book-id") Integer bookId,
            Authentication connectUser
    ) {
        return ResponseEntity.ok(bookService.updateArchivedStatus(bookId, connectUser));
    }

    @PostMapping("/borrow/{book-id}")
    public ResponseEntity<Integer> borrowBook(
            @PathVariable("book-id") Integer bookId,
            Authentication connectUser
    ) {
        return ResponseEntity.ok(bookService.borrowBook(bookId, connectUser));
    }

    @PatchMapping("/borrow/return/{book-id}")
    public ResponseEntity<Integer> returnBorrowBook(
            @PathVariable("book-id") Integer bookId,
            Authentication connectUser
    ) {
        return ResponseEntity.ok(bookService.returnBorrowBook(bookId, connectUser));
    }

    @PatchMapping("/borrow/return/approve/{book-id}")
    public ResponseEntity<Integer> approveReturnBorrowBook(
            @PathVariable("book-id") Integer bookId,
            Authentication connectUser
    ) {
        return ResponseEntity.ok(bookService.approveReturnBorrowBook(bookId, connectUser));
    }

    @PostMapping(value = "/cover/{book-id}", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadBookCoverPicture(
            @PathVariable("book-id") Integer bookId,
            @Parameter()
            @RequestPart("file") MultipartFile file,
            Authentication connectUser
    ) {
        bookService.uploadBookCoverPicture(file, bookId, connectUser);
        return ResponseEntity.accepted().build();
    }
}
