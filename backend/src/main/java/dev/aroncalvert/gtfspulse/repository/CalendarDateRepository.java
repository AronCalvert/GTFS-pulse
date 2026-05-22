package dev.aroncalvert.gtfspulse.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.aroncalvert.gtfspulse.entity.CalendarDate;
import dev.aroncalvert.gtfspulse.entity.CalendarDateId;

@Repository
public interface CalendarDateRepository extends JpaRepository<CalendarDate, CalendarDateId> {
  List<CalendarDate> findByDate(LocalDate date);
}
