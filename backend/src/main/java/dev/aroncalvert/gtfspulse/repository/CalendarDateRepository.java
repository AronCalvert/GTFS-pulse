package dev.aroncalvert.gtfspulse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.aroncalvert.gtfspulse.entity.CalendarDate;

@Repository
public interface CalendarDateRepository extends JpaRepository<CalendarDate, String> {
}
