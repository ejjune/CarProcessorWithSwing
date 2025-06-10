package org.blank.repository;

import java.util.ArrayList;
import java.util.List;
import org.blank.repository.model.Car;

public class CarRepository {

  private static List<Car> cars;

  private CarRepository() {}

  static {
    cars = List.of();
  }

  public static void addCars(List<Car> newCars) {
    cars = List.copyOf(newCars);
  }

  public static List<Car> getAllCars() {
    return cars;
  }

  public static boolean deleteAllCars() {
    cars = new ArrayList<>();
    return true;
  }
}
