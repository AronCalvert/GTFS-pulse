package dev.aroncalvert.gtfspulse.repository;

import java.util.Calendar;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, String> {
  Optional<Calendar> findByServiceId(String serviceId);
}
