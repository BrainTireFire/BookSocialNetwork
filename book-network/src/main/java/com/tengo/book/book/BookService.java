package com.tengo.book.book;

import com.tengo.book.common.PagedResponse;
import com.tengo.book.exception.OperationNotPermittedException;
import com.tengo.book.file.FileStorageService;
import com.tengo.book.history.BookTransactionHistory;
import com.tengo.book.history.BookTransactionHistoryRepository;
import com.tengo.book.notification.Notification;
import com.tengo.book.notification.NotificationService;
import com.tengo.book.notification.NotificationStatus;
import com.tengo.book.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookMapper bookMapper;
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository transactionHistoryRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public Integer saveBook(BookRequest bookRequest, Authentication connectUser) {
        User user = getAuthenticatedUser(connectUser);
        Book book = bookMapper.toBook(bookRequest);
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }

    public BookResponse findById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));
    }

    public PagedResponse<BookResponse> findAllBooks(int page, int size, Authentication connectUser) {
        User user = getAuthenticatedUser(connectUser);
        Pageable pageable = createPageRequest(page, size);
        Page<Book> books = bookRepository.findAllDisplayableBooks(pageable, user.getId());
        return toPagedResponse(books, bookMapper::toBookResponse);
    }

    public PagedResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectUser) {
        User user = getAuthenticatedUser(connectUser);
        Pageable pageable = createPageRequest(page, size);
        Page<Book> books = bookRepository.findAll(BookSpecification.withOwnerId(user.getId()), pageable);
        return toPagedResponse(books, bookMapper::toBookResponse);
    }

    public PagedResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectUser) {
        User user = getAuthenticatedUser(connectUser);
        Pageable pageable = createPageRequest(page, size);
        Page<BookTransactionHistory> borrowedBooks = transactionHistoryRepository.findAllBorrowedBooks(pageable, user.getId());
        return toPagedResponse(borrowedBooks, bookMapper::toBorrowedBookResponse);
    }

    public PagedResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectUser) {
        User user = getAuthenticatedUser(connectUser);
        Pageable pageable = createPageRequest(page, size);
        Page<BookTransactionHistory> returnedBooks = transactionHistoryRepository.findAllReturnedBooks(pageable, user.getId());
        return toPagedResponse(returnedBooks, bookMapper::toBorrowedBookResponse);
    }

    public Integer updateShareableStatus(Integer bookId, Authentication connectUser) {
        Book book = validateBookOwnership(bookId, connectUser);
        book.setShareable(!book.isShareable());
        return bookRepository.save(book).getId();
    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectUser) {
        Book book = validateBookOwnership(bookId, connectUser);
        book.setArchived(!book.isArchived());
        return bookRepository.save(book).getId();
    }

    public Integer borrowBook(Integer bookId, Authentication connectUser) {
        Book book = findBookById(bookId);
        validateBorrowableBook(book);

        User user = getAuthenticatedUser(connectUser);
        validateBorrowingConditions(book, user);

        BookTransactionHistory transaction = BookTransactionHistory.builder()
                .book(book)
                .user(user)
                .returned(false)
                .returnApproved(false)
                .build();

        var saved = transactionHistoryRepository.save(transaction);
        notificationService.sendNotification(
                book.getCreatedBy().toString(),
                Notification.builder()
                        .status(NotificationStatus.BORROWED)
                        .message("Your book " + book.getTitle() + " has been borrowed by " + user.getUsername())
                        .bookTitle(book.getTitle())
                        .build()
        );
        return saved.getId();
    }

    public Integer returnBorrowBook(Integer bookId, Authentication connectUser) {
        Book book = findBookById(bookId);
        validateReturnableBook(book);

        User user = getAuthenticatedUser(connectUser);
        BookTransactionHistory transaction = transactionHistoryRepository.findByBookIdAndUserId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("You have not borrowed this book"));

        transaction.setReturned(true);

        var saved = transactionHistoryRepository.save(transaction);
        notificationService.sendNotification(
                book.getCreatedBy(),
                Notification.builder()
                        .status(NotificationStatus.RETURNED)
                        .message("Your book " + book.getTitle() + " has been returned by " + user.getUsername())
                        .bookTitle(book.getTitle())
                        .build()
        );
        return saved.getId();
    }

    public Integer approveReturnBorrowBook(Integer bookId, Authentication connectUser) {
        Book book = findBookById(bookId);
        validateReturnableBook(book);

        User user = getAuthenticatedUser(connectUser);
        BookTransactionHistory transaction = transactionHistoryRepository.findByBookIdAndOwnerId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("You have not borrowed this book"));

        transaction.setReturnApproved(true);

        var saved = transactionHistoryRepository.save(transaction);
        notificationService.sendNotification(
                transaction.getCreatedBy(),
                Notification.builder()
                        .status(NotificationStatus.RETURN_APPROVED)
                        .message("Your book " + book.getTitle() + " has been approved")
                        .bookTitle(book.getTitle())
                        .build()
        );
        return saved.getId();
    }

    // Helper methods for common actions

    private User getAuthenticatedUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    private Book findBookById(Integer bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + bookId));
    }

    private Book validateBookOwnership(Integer bookId, Authentication authentication) {
        Book book = findBookById(bookId);
        User user = getAuthenticatedUser(authentication);

        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You are not the owner of this book");
        }

        return book;
    }

    private Pageable createPageRequest(int page, int size) {
        return PageRequest.of(page, size, Sort.by("createdDate").descending());
    }

    private void validateBorrowableBook(Book book) {
        if (!book.isShareable() || book.isArchived()) {
            throw new OperationNotPermittedException("The requested book is not shareable or archived");
        }
    }

    private void validateReturnableBook(Book book) {
        if (!book.isShareable() || book.isArchived()) {
            throw new OperationNotPermittedException("The requested book is not shareable or archived");
        }
    }

    private void validateBorrowingConditions(Book book, User user) {
        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow your own book");
        }

        if (transactionHistoryRepository.isAlreadyBorrowedByUser(book.getId(), user.getId())) {
            throw new OperationNotPermittedException("You already borrowed this book and it is still not returned or the return is not approved by the owner");
        }

        if (transactionHistoryRepository.isAlreadyBorrowed(book.getId())) {
            throw new OperationNotPermittedException("The requested book is already borrowed");
        }
    }

    private <T, R> PagedResponse<R> toPagedResponse(Page<T> page, Function<T, R> mapper) {
        List<R> responses = page.stream().map(mapper).toList();

        return new PagedResponse<>(
                responses,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    public void uploadBookCoverPicture(MultipartFile file, Integer bookId, Authentication connectUser) {
        Book book = findBookById(bookId);
        User user = getAuthenticatedUser(connectUser);

        var bookCover = fileStorageService.saveFile(file, user.getId());
        book.setBookCover(bookCover);
        bookRepository.save(book);
    }
}
