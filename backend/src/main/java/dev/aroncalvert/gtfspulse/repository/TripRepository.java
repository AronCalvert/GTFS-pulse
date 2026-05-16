package dev.aroncalvert.gtfspulse.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.aroncalvert.gtfspulse.entity.Trip;

@Repository
public interface TripRepository extends JpaRepository<Trip, String> {
  List<Trip> findByRouteId(String routeId);

  List<Trip> findByServiceId(String serviceId);
}
