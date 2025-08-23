package org.example.gundokai.mapper;

import org.example.gundokai.dto.request.NotificationsRequest;
import org.example.gundokai.dto.respone.NotificationsResponse;
import org.example.gundokai.entity.Notifications;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationsMapper {
    Notifications toNotifications(NotificationsRequest notificationsRequest);

    @Mapping(target = "user_id", source = "user.id")
    NotificationsResponse toNotificationsResponse(Notifications notifications);
}
