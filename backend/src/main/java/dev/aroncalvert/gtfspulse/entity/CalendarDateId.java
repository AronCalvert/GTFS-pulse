package dev.aroncalvert.gtfspulse.entity;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor
public class CalendarDateId implements Serializable {
  private String serviceId;

  private LocalDate date;

  public CalendarDateId(String serviceId, LocalDate date) {
    this.serviceId = serviceId;
    this.date = date;
  }
}
