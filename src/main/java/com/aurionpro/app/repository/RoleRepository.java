package com.aurionpro.app.repository;

import com.aurionpro.app.entity.Role;
import com.aurionpro.app.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}