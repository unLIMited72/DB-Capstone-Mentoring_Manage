package com.mentoring.mentoringbackend.notification.web;

import com.mentoring.mentoringbackend.common.dto.ApiResponse;
import com.mentoring.mentoringbackend.notification.dto.NotificationResponse;
import com.mentoring.mentoringbackend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 내 알림 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications() {
        List<NotificationResponse> notifications = notificationService.getMyNotifications();
        // 🔽 여기 수정
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * 알림 읽음 처리
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        // 🔽 여기도 수정
        return ResponseEntity.ok(ApiResponse.success(null));
        // 혹은 ApiResponse.success(null) 대신 ApiResponse.success((Void) null) 써도 됨
    }
}
