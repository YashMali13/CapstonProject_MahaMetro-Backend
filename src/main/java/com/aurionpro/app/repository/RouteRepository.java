package com.aurionpro.app.repository;

import com.aurionpro.app.entity.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {

    @Query("SELECT r FROM Route r LEFT JOIN FETCH r.stations WHERE r.id = :id AND r.deleted = false")
    Optional<Route> findByIdAndDeletedFalse(Long id);
    
    Page<Route> findAllByDeletedFalse(Pageable pageable);

    boolean existsByNameAndDeletedFalse(String name);
    
    boolean existsByIdAndDeletedFalse(Long id);

    List<Route> findAllByDeletedTrue();
    
    Optional<Route> findByIdAndDeletedTrue(Long id);

    @Modifying
    @Query("DELETE FROM Route r WHERE r.id = :id")
    void hardDeleteById(Long id);
}