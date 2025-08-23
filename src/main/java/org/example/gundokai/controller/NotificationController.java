package org.example.gundokai.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.gundokai.dto.respone.ApiResponse;
import org.example.gundokai.dto.respone.NotificationsResponse;
import org.example.gundokai.service.NotificationsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification/")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {
    NotificationsService notificationsService;


    @PreAuthorize("hasAuthority('GET_NOTIFICATIONS')")
    @GetMapping("/getAll")
    public ApiResponse<List<NotificationsResponse>> getNotifications(){
        ApiResponse<List<NotificationsResponse>> response = new ApiResponse<>();
        response.setMessage("Get Notifications");
        response.setResult(notificationsService.getAllNotifications());
        return response;
    }
    @PreAuthorize("hasAuthority('READ_NOTIFICATIONS')")
    @PutMapping("/markAsRead/{notificationId}")
    public ApiResponse<String> markAsRead(@PathVariable String notificationId){
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Mark As Read");
        response.setResult(notificationsService.markAsRead(notificationId));
        return response;
    }
}
