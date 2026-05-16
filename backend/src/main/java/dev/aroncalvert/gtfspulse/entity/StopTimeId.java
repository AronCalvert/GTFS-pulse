package dev.aroncalvert.gtfspulse.entity;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor
public class StopTimeId implements Serializable {
  private String trip;

  private int stopSequence;

  public StopTimeId(String trip, int stopSequence) {
    this.trip = trip;
    this.stopSequence = stopSequence;
  }
}
