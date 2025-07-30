package org.example.gundokai.mapper;

import org.example.gundokai.dto.request.RoleRequest;
import org.example.gundokai.dto.respone.RoleResponse;
import org.example.gundokai.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
