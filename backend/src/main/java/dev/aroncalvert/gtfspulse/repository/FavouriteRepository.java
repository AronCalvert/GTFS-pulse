package dev.aroncalvert.gtfspulse.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.aroncalvert.gtfspulse.entity.Favourite;

public interface FavouriteRepository extends JpaRepository<Favourite, Long> {
  List<Favourite> findByUserId(Long userId);
}
