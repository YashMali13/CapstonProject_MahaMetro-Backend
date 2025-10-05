package com.aurionpro.app.repository;

import com.aurionpro.app.entity.Station;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Long> {

    Optional<Station> findByIdAndDeletedFalse(Long id);
    
    Page<Station> findAllByDeletedFalse(Pageable pageable);
    
    @Query("SELECT s FROM Station s LEFT JOIN FETCH s.routes WHERE s.deleted = false")
    List<Station> findAllWithRoutes();

    List<Station> findAllByIdInAndDeletedFalse(List<Long> ids);
    
    boolean existsByNameAndDeletedFalse(String name);
    
    boolean existsByIdAndDeletedFalse(Long id);
    
    List<Station> findAllByDeletedTrue();
    
    Optional<Station> findByIdAndDeletedTrue(Long id);

    @Modifying
    @Query("DELETE FROM Station s WHERE s.id = :id")
    void hardDeleteById(Long id);
}