package com.healthcare.repository;

import com.healthcare.domain.PlanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlanItemRepository extends JpaRepository<PlanItem, Long> {

    List<PlanItem> findByDayIdOrderByCreatedAtAsc(Long dayId);

    @Query("""
           select d.id from PlanDay d 
           join PlanItem i on i.dayId = d.id
           where i.id = :itemId
           """)
    Optional<Long> findDayIdByItemId(Long itemId);

    @Query("""
           select w.matchId from PlanWeek w
           join PlanDay d on d.weekId = w.id
           join PlanItem i on i.dayId = d.id
           where i.id = :itemId
           """)
    Optional<Long> findMatchIdByItemId(Long itemId);
}
