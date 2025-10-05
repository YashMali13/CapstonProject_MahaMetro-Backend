package com.aurionpro.app.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aurionpro.app.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    
    
    Page<User> findAllByDeletedFalse(Pageable pageable);
    
    Page<User> findAllByDeletedTrue(Pageable pageable);
    
    Optional<User> findByIdAndDeletedFalse(Long id);
    
    Optional<User> findByIdAndDeletedTrue(Long id);

    boolean existsByIdAndDeletedFalse(Long id);

    @Modifying
    @Query("DELETE FROM User u WHERE u.id = :id")
    void hardDeleteById(@Param("id") Long id);
    
    long countByCreatedAtAfter(Instant startOfDay);
}