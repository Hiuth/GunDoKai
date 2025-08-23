package org.example.gundokai.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.dto.request.NotificationsRequest;
import org.example.gundokai.dto.respone.NotificationsResponse;
import org.example.gundokai.entity.Notifications;
import org.example.gundokai.entity.User;
import org.example.gundokai.exception.AppException;
import org.example.gundokai.exception.ErrorCode;
import org.example.gundokai.mapper.NotificationsMapper;
import org.example.gundokai.repository.NotificationsRepository;
import org.example.gundokai.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class NotificationsService {
    NotificationsRepository notificationsRepository;
    NotificationsMapper notificationsMapper;
    UserRepository userRepository;

    private String getAccountIdFromContext() {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(
                ()-> new AppException(ErrorCode.USER_NOT_EXISTED)
        );
        return user.getId();
    }
    public NotificationsResponse createNotification(NotificationsRequest notificationsRequest) {
        User user = userRepository.findByEmail(notificationsRequest.getEmail()).orElseThrow(
                ()-> new AppException(ErrorCode.USER_NOT_EXISTED)
        );
        Notifications notifications = notificationsMapper.toNotifications(notificationsRequest);
        notifications.set_read(false);
        notifications.setUser(user);
        notifications.setMessage(notificationsRequest.getMessage());
        return notificationsMapper.toNotificationsResponse( notificationsRepository.save(notifications));
    }

    public List<NotificationsResponse> getAllNotifications() {
        List<Notifications> notifications = notificationsRepository.findAllByUserId(getAccountIdFromContext());
        return notifications.stream().map(notificationsMapper::toNotificationsResponse).collect(Collectors.toList());
    }

    public String markAsRead(String notificationId) {
        Notifications notifications = notificationsRepository.findById(notificationId).orElseThrow(
                ()-> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND)
        );
        notifications.set_read(true);
        notificationsRepository.save(notifications);
        return "Marked as read successfully";
    }
}
