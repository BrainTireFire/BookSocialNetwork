package com.tengo.book.feedback;

import com.tengo.book.common.PagedResponse;
import org.springframework.security.core.Authentication;

public interface FeedbackService {
    Integer save(FeedbackRequest feedBackRequest, Authentication connectUser);
    PagedResponse<FeedbackResponse> findAllFeedbackByBook(Integer bookId, int page, int size, Authentication connectUser);
}
