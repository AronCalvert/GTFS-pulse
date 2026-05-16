package dev.aroncalvert.gtfspulse.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import dev.aroncalvert.gtfspulse.entity.Calendar;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, String> {
  Optional<Calendar> findByServiceId(String serviceId);
}
