package dev.aroncalvert.gtfspulse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.aroncalvert.gtfspulse.entity.Agency;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, String> {

}
