package dev.aroncalvert.gtfspulse.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue
  private Long id;

  private String name;

  @Column(unique = true)
  private String email;

  private String hashedPassword;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @Enumerated(EnumType.STRING)
  private Role role;
}
