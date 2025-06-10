package org.blank.repository.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class CarTest {

  /**
   * Tests the {@link Car#addPrice(String, double)} method when additionalPrices map is initially
   * null. Verifies that the map is initialized and the price is added correctly.
   */
  @Test
  void addPrice_initializesMapAndAddsPrice() {
    Car car =
        Car.builder()
            .brand("TestBrand")
            .type("TestType")
            .model("TestModel")
            .price(100.0)
            .currency("USD")
            .releaseDate(LocalDate.now())
            .build(); // additionalPrices should be null initially

    assertNull(
        car.getAdditionalPrices(), "additionalPrices should be null before calling addPrice");

    car.addPrice("EUR", 90.0);

    assertNotNull(
        car.getAdditionalPrices(), "additionalPrices should be initialized after calling addPrice");
    assertEquals(1, car.getAdditionalPrices().size(), "Map should contain one entry");
    assertEquals(90.0, car.getAdditionalPrices().get("EUR"), "Price should be added correctly");
  }

  /**
   * Tests the {@link Car#addPrice(String, double)} method when additionalPrices map already exists.
   * Verifies that new prices are added and existing prices are updated correctly.
   */
  @Test
  void addPrice_addsToExistingMap() {
    Map<String, Double> initialPrices = new HashMap<>();
    initialPrices.put("GBP", 80.0);

    Car car = Car.builder().brand("TestBrand").additionalPrices(initialPrices).build();

    assertNotNull(car.getAdditionalPrices(), "additionalPrices should not be null");
    assertEquals(1, car.getAdditionalPrices().size(), "Map should contain initial entry");

    car.addPrice("EUR", 95.0);
    car.addPrice("GBP", 85.0); // Update existing

    assertEquals(2, car.getAdditionalPrices().size(), "Map should contain two entries");
    assertEquals(95.0, car.getAdditionalPrices().get("EUR"), "New price should be added");
    assertEquals(85.0, car.getAdditionalPrices().get("GBP"), "Existing price should be updated");
  }

  /** Tests the {@link Car.CarBuilder} for basic construction and default values. */
  @Test
  void builder_createsCarCorrectly() {
    LocalDate testDate = LocalDate.of(2023, 10, 26);
    Car car =
        Car.builder()
            .brand("Tesla")
            .type("Sedan")
            .model("Model 3")
            .price(50000.0)
            .currency("USD")
            .releaseDate(testDate)
            .additionalPrices(Map.of("CAD", 65000.0))
            .build();

    assertEquals("Tesla", car.getBrand());
    assertEquals("Sedan", car.getType());
    assertEquals("Model 3", car.getModel());
    assertEquals(50000.0, car.getPrice());
    assertEquals("USD", car.getCurrency());
    assertEquals(testDate, car.getReleaseDate());
    assertNotNull(car.getAdditionalPrices());
    assertEquals(1, car.getAdditionalPrices().size());
    assertTrue(car.getAdditionalPrices().containsKey("CAD"));
  }

  /** Tests the {@link Car} object's default values when not explicitly set by the builder. */
  @Test
  void builder_withMinimalFields() {
    Car car = Car.builder().brand("MinimalCar").build();

    assertEquals("MinimalCar", car.getBrand());
    assertNull(car.getType());
    assertNull(car.getModel());
    assertEquals(0.0, car.getPrice()); // double primitive defaults to 0.0
    assertNull(car.getCurrency());
    assertNull(car.getReleaseDate());
    assertNull(car.getAdditionalPrices());
  }
}
