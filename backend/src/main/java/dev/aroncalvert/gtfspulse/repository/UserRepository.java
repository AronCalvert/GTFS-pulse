package dev.aroncalvert.gtfspulse.repository;

import java.util.Optional;
import dev.aroncalvert.gtfspulse.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
}
