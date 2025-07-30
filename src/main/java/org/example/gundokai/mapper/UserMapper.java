package org.example.gundokai.mapper;

import org.example.gundokai.dto.request.UserCreationRequest;
import org.example.gundokai.dto.request.UserUpdateRequest;
import org.example.gundokai.dto.respone.UserResponse;
import org.example.gundokai.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
