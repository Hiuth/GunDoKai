package org.example.gundokai.mapper;

import org.example.gundokai.dto.request.PermissionRequest;
import org.example.gundokai.dto.respone.PermissionResponse;
import org.example.gundokai.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
