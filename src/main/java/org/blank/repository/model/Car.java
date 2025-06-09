package org.blank.repository.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Car {

  private String brand;

  private String type;

  private String model;

  private double price;

  private String currency;

  private LocalDate releaseDate;

  private Map<String, Double> additionalPrices;

  public void addPrice(String currency, double price) {
    if (additionalPrices == null) additionalPrices = new HashMap<>();

    this.additionalPrices.put(currency, price);
  }
}
