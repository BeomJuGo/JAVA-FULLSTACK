package com.healthcare.repository;

import com.healthcare.domain.PlanWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface PlanWeekRepository extends JpaRepository<PlanWeek, Long> {

    Optional<PlanWeek> findByMatchIdAndWeekStart(Long matchId, LocalDate weekStart);

    @Query("select w.matchId from PlanWeek w where w.id = :weekId")
    Optional<Long> findMatchIdByWeekId(Long weekId);
}
