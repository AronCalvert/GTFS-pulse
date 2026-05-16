package dev.aroncalvert.gtfspulse.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.aroncalvert.gtfspulse.entity.Route;

@Repository
public interface RouteRepository extends JpaRepository<Route, String> {
  Optional<Route> findByRouteShortName(String routeShortName);
}
