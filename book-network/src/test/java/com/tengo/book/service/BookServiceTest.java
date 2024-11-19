package com.tengo.book.service;

import com.tengo.book.book.*;
import com.tengo.book.common.PagedResponse;
import com.tengo.book.history.BookTransactionHistory;
import com.tengo.book.history.BookTransactionHistoryRepository;
import com.tengo.book.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;



@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookTransactionHistoryRepository transactionHistoryRepository;
    @Mock
    private BookMapper bookMapper;
    @InjectMocks
    private BookService bookService;

    @Test
    void shouldReturnBookResponseWhenBookExists() {
        //Arrange
        Integer bockId = 1;
        Book book = new Book();
        book.setId(bockId);
        BookResponse bookResponse = new BookResponse();
        bookResponse.setId(bockId);

        Mockito.when(bookRepository.findById(bockId)).thenReturn(Optional.of(book));
        Mockito.when(bookMapper.toBookResponse(book)).thenReturn(bookResponse);

        //Act
        BookResponse response = bookService.findById(bockId);

        //Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(bockId, response.getId());
        Mockito.verify(bookRepository).findById(bockId);
        Mockito.verify(bookMapper).toBookResponse(book);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenBookDoesNotExist() {
        //Arrange
        Integer bockId = 1;
        Mockito.when(bookRepository.findById(bockId)).thenReturn(Optional.empty());

        //Act
        EntityNotFoundException exception = Assertions.assertThrows(EntityNotFoundException.class, () ->  {
            bookService.findById(bockId);
        });

        //Assert
        Assertions.assertEquals("Book not found with id: " + bockId, exception.getMessage());
        Mockito.verify(bookRepository).findById(bockId);
        Mockito.verifyNoInteractions(bookMapper);
    }

    @Test
    void shouldSaveBookAndReturnId() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //Arrange
        BookRequest bookRequest = new BookRequest(
                null,
                "title",
                "authorName",
                "isbn",
                "synopsis",
                true
        );
        Authentication authentication = Mockito.mock(Authentication.class);
        User user = new User();

        Book book = new Book();
        book.setId(1);
        book.setOwner(user);

        //private method
        Method getAuthenticatedUser = BookService.class
                .getDeclaredMethod("getAuthenticatedUser", Authentication.class);
        getAuthenticatedUser.setAccessible(true);

        Mockito.when(bookMapper.toBook(bookRequest)).thenReturn(book);
        Mockito.when(bookRepository.save(book)).thenReturn(book);
        Mockito.when(getAuthenticatedUser.invoke(bookService, authentication)).thenReturn(user);

        Integer result = bookService.saveBook(bookRequest, authentication);

        Assertions.assertEquals(1, result);
        Mockito.verify(bookMapper).toBook(bookRequest);
        Mockito.verify(bookRepository).save(book);
        Mockito.verify(authentication).getPrincipal();
    }

    @Test
    void shouldFindAllBorrowedBooks() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        int page = 0;
        int size = 10;
        Authentication authentication = Mockito.mock(Authentication.class);
        User user = new User();
        user.setId(1);

        Method createPageRequest = BookService.class
                .getDeclaredMethod("createPageRequest", int.class, int.class);
        createPageRequest.setAccessible(true);

        Method getAuthenticatedUser = BookService.class
                .getDeclaredMethod("getAuthenticatedUser", Authentication.class);
        getAuthenticatedUser.setAccessible(true);

        Pageable pageable = (Pageable) createPageRequest.invoke(bookService, page, size);
        Page<BookTransactionHistory> borrowedBooks = new PageImpl<>(List.of(new BookTransactionHistory()));
        BorrowedBookResponse borrowedBookResponse = new BorrowedBookResponse();

        Mockito.when(getAuthenticatedUser.invoke(bookService, authentication)).thenReturn(user);
        Mockito.when(transactionHistoryRepository.findAllBorrowedBooks(pageable, user.getId()))
                .thenReturn(borrowedBooks);
        Mockito.when(bookMapper.toBorrowedBookResponse(Mockito.any(BookTransactionHistory.class)))
                .thenReturn(borrowedBookResponse);

        // Act
        PagedResponse<BorrowedBookResponse> result = bookService.findAllBorrowedBooks(page, size, authentication);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getContent().size());
        Mockito.verify(transactionHistoryRepository).findAllBorrowedBooks(pageable, user.getId());
    }
}
