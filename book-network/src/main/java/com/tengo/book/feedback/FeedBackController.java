package com.tengo.book.feedback;

import com.tengo.book.common.PagedResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("feedbacks")
@RequiredArgsConstructor
@Tag(name = "FeedBack", description = "FeedBack API")
public class FeedBackController {

    private final FeedBackService feedBackService;

    @PostMapping
    public ResponseEntity<Integer> saveFeedBack(
            @RequestBody @Valid FeedbackRequest feedBackRequest,
            Authentication connectUser
    ) {
        return ResponseEntity.ok(feedBackService.save(feedBackRequest, connectUser));
    }

    @GetMapping("/book/{book-id}")
    public ResponseEntity<PagedResponse<FeedbackResponse>> findAllFeedbackByBook(
            @PathVariable("book-id") Integer bookId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectUser
    ) {
        return ResponseEntity.ok(feedBackService.findAllFeedbackByBook(bookId, page, size, connectUser));
    }
}
