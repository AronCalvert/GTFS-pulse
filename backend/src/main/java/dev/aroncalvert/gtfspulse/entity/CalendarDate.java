package dev.aroncalvert.gtfspulse.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Setter;

@Entity
@Table(name = "calendar_dates")
@IdClass(CalendarDateId.class)
@Setter
public class CalendarDate {
  @Id
  @Column(name = "service_id")
  String serviceId;

  @Id
  LocalDate date;

  @Column(name = "exception_type")
  int exceptionType;
}
