package dev.aroncalvert.gtfspulse.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import dev.aroncalvert.gtfspulse.entity.Calendar;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, String> {
  Optional<Calendar> findByServiceId(String serviceId);

  List<Calendar> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate today, LocalDate today2);
}
