package com.aurionpro.app.repository;

import com.aurionpro.app.entity.RefreshToken;
import com.aurionpro.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    /**
     * Finds all refresh tokens for a given user.
     * The service layer will then iterate through these to find a bcrypt match.
     */
    List<RefreshToken> findByUser(User user);
}