package com.tengo.book.feedback;

import com.tengo.book.book.Book;
import com.tengo.book.book.BookRepository;
import com.tengo.book.common.PagedResponse;
import com.tengo.book.exception.OperationNotPermittedException;
import com.tengo.book.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final BookRepository bookRepository;
    private final FeedbackMapper feedbackMapper;
    private final FeedbackRepository feedbackRepository;

    public Integer save(FeedbackRequest feedBackRequest, Authentication connectUser) {
        Book book = bookRepository.findById(feedBackRequest.bookId())
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + feedBackRequest.bookId()));

        if (!book.isShareable() || book.isArchived()) {
            throw new OperationNotPermittedException("You cannot give a feedback for an archived or unshareable book");
        }

        User user = ((User) connectUser.getPrincipal());
        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot give a feedback for your own book");
        }

        Feedback feedback = feedbackMapper.toFeedback(feedBackRequest);

        return feedbackRepository.save(feedback).getId();
    }

    public PagedResponse<FeedbackResponse> findAllFeedbackByBook(Integer bookId, int page, int size, Authentication connectUser) {
        Pageable pageable = PageRequest.of(page, size);
        User user = ((User) connectUser.getPrincipal());
        Page<Feedback> feedbacks = feedbackRepository.findAllByBookId(bookId, pageable);
        List<FeedbackResponse> feedbackResponses = feedbacks.stream()
                .map(f -> feedbackMapper.toFeedbackResponse(f, user.getId()))
                .toList();

        return new PagedResponse<>(
                feedbackResponses,
                feedbacks.getNumber(),
                feedbacks.getSize(),
                feedbacks.getTotalElements(),
                feedbacks.getTotalPages(),
                feedbacks.isFirst(),
                feedbacks.isLast()
        );
    }
}
