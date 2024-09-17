package com.tengo.book.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public void sendNotification(String userId, Notification notification) {
        log.info("Sending notification to user: {}", userId);
        simpMessagingTemplate.convertAndSendToUser(userId, "/notification", notification);
    }
}
