package com.tengo.book.book;

import com.tengo.book.common.PagedResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {

    Integer saveBook(BookRequest bookRequest, Authentication connectUser);

    BookResponse findById(Integer bookId);

    PagedResponse<BookResponse> findAllBooks(int page, int size, Authentication connectUser);

    PagedResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectUser);

    PagedResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectUser);

    PagedResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectUser);

    Integer updateShareableStatus(Integer bookId, Authentication connectUser);

    Integer updateArchivedStatus(Integer bookId, Authentication connectUser);

    Integer borrowBook(Integer bookId, Authentication connectUser);

    Integer returnBorrowBook(Integer bookId, Authentication connectUser);

    Integer approveReturnBorrowBook(Integer bookId, Authentication connectUser);

    void uploadBookCoverPicture(MultipartFile file, Integer bookId, Authentication connectUser);
}
