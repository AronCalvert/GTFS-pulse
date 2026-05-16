package dev.aroncalvert.gtfspulse.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "calendar")
public class Calendar {

  @Column(name = "service_id")
  @Id
  String serviceId;

  int monday;
  int tuesday;
  int wednesday;
  int thursday;
  int friday;
  int saturday;
  int sunday;

  @Column(name = "start_date")
  LocalDate startDate;

  @Column(name = "end_date")
  LocalDate endDate;
}
