package dev.aroncalvert.gtfspulse.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Favourite {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "user_id")
  private long userId;

  @Column(name = "stop_id")
  private String stopId;

  @Nullable
  @Column(name = "route_id")
  private String routeId;
}
