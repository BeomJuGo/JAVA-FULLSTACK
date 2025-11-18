package com.healthcare.repository;

import com.healthcare.domain.PlanDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlanDayRepository extends JpaRepository<PlanDay, Long> {

    List<PlanDay> findByWeekIdOrderByDayIndexAsc(Long weekId);

    @Query("""
           select w.id from PlanWeek w 
           join PlanDay d on d.weekId = w.id
           where d.id = :dayId
           """)
    Optional<Long> findWeekIdByDayId(Long dayId);

    @Query("""
           select w.matchId from PlanWeek w 
           join PlanDay d on d.weekId = w.id
           where d.id = :dayId
           """)
    Optional<Long> findMatchIdByDayId(Long dayId);
}
