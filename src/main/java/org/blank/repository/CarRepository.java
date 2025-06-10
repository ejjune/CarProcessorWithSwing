package org.blank.repository;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.blank.repository.model.Car;

public class CarRepository {

  private static List<Car> cars;

  @Getter private static List<Car> filteredCars;

  private CarRepository() {}

  static {
    cars = List.of();
  }

  public static void addCars(List<Car> newCars) {
    cars = List.copyOf(newCars);
    filteredCars = List.copyOf(newCars);
  }

  public static List<Car> getAllCars() {
    return cars;
  }

  public static boolean deleteAllCars() {
    cars = new ArrayList<>();
    return true;
  }

  public static void sortOrFilterCars(List<Car> newCars) {
    filteredCars = List.copyOf(newCars);
  }
}
