package com.aurionpro.app.repository;

import com.aurionpro.app.entity.FareRule;
import com.aurionpro.app.entity.TicketType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface FareRuleRepository extends JpaRepository<FareRule, Long> {

    Optional<FareRule> findByTicketTypeAndMinStationCountLessThanEqualAndMaxStationCountGreaterThanEqualAndDeletedFalse(
            TicketType ticketType, int stationCount, int stationCount2);

    Optional<FareRule> findByTicketTypeAndDeletedFalse(TicketType ticketType);

    Optional<FareRule> findByIdAndDeletedFalse(Long id);

    Page<FareRule> findAllByDeletedFalse(Pageable pageable);
    
    List<FareRule> findAllByDeletedTrue();
    
    Optional<FareRule> findByIdAndDeletedTrue(Long id);
    
    @Modifying
    @Query("DELETE FROM FareRule fr WHERE fr.id = :id")
    void hardDeleteById(Long id);
}