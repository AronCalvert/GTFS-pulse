package dev.aroncalvert.gtfspulse.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.aroncalvert.gtfspulse.entity.Stop;

@Repository
public interface StopRepository extends JpaRepository<Stop, String> {
  Optional<Stop> findByStopCode(String stopCode);

  Optional<Stop> findByStopName(String stopName);
}
