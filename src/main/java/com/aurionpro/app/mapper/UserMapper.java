package com.aurionpro.app.mapper;

import com.aurionpro.app.dto.ProfileResponse;
import com.aurionpro.app.dto.UserResponse;
import com.aurionpro.app.entity.Role;
import com.aurionpro.app.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles")
    UserResponse toResponse(User user);

    @Mapping(target = "roles", source = "roles")
    ProfileResponse toProfileResponse(User user);

    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}