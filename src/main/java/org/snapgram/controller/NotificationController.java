package org.snapgram.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.response.NotificationDTO;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.service.notification.INotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/notifications")
@Validated
public class NotificationController {
    INotificationService notificationService;

    @GetMapping
    public ResponseObject<List<NotificationDTO>> getNotifications(
            @AuthenticationPrincipal CustomUserSecurity user,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(0) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "30") @Min(0) @Max(50) Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize).withSort(Sort.by(Sort.Order.desc("createdAt")));
        return new ResponseObject<>(HttpStatus.OK, notificationService.getNotificationsByUser(user.getId(), pageable));
    }

    @GetMapping("/is-read")
    public ResponseObject<Boolean> getIsRead(@AuthenticationPrincipal CustomUserSecurity user) {
        return new ResponseObject<>(HttpStatus.OK, notificationService.isRead(user.getId()));
    }

    @PutMapping
    public ResponseObject<Void> markNotificationAsRead(@AuthenticationPrincipal CustomUserSecurity user) {
        notificationService.markAsRead(user.getId());
        return new ResponseObject<>(HttpStatus.OK, null);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseObject<Void> deleteNotification(@PathVariable UUID notificationId) {
        notificationService.deleteNotification(notificationId);
        return new ResponseObject<>(HttpStatus.OK, null);
    }
}
