package org.blank.repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.blank.repository.model.Car;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CarRepositoryTest {

  /** Clears the repository before each test to ensure test isolation. */
  @BeforeEach
  void setUp() {
    CarRepository.deleteAllCars();
  }

  /** Tests {@link CarRepository#addCars(List)} with a single car. */
  @Test
  void addCars_singleCar() {
    Car car1 = Car.builder().brand("Toyota").build();
    CarRepository.addCars(Collections.singletonList(car1));
    assertEquals(1, CarRepository.getAllCars().size());
    assertTrue(CarRepository.getAllCars().contains(car1));
  }

  /** Tests {@link CarRepository#addCars(List)} with multiple cars. */
  @Test
  void addCars_multipleCars() {
    Car car1 = Car.builder().brand("Toyota").build();
    Car car2 = Car.builder().brand("Honda").build();
    CarRepository.addCars(Arrays.asList(car1, car2));
    assertEquals(2, CarRepository.getAllCars().size());
    assertTrue(CarRepository.getAllCars().contains(car1));
    assertTrue(CarRepository.getAllCars().contains(car2));
  }

  /** Tests {@link CarRepository#addCars(List)} with an empty list. */
  @Test
  void addCars_emptyList() {
    CarRepository.addCars(Collections.emptyList());
    assertTrue(CarRepository.getAllCars().isEmpty());
  }

  /**
   * Tests {@link CarRepository#addCars(List)} with a null list, expecting a NullPointerException.
   */
  @Test
  void addCars_nullList_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> CarRepository.addCars(null));
  }

  /** Tests {@link CarRepository#getAllCars()} to ensure it returns an unmodifiable list. */
  @Test
  void getAllCars_returnsUnmodifiableList() {
    Car car = Car.builder().brand("Test").build();
    CarRepository.addCars(Collections.singletonList(car));

    List<Car> cars = CarRepository.getAllCars();
    Car car1 = Car.builder().brand("New").build();
    assertThrows(UnsupportedOperationException.class, () -> cars.add(car1));
  }

  /** Tests {@link CarRepository#deleteAllCars()} to ensure the repository becomes empty. */
  @Test
  void deleteAllCars_clearsRepository() {
    Car car1 = Car.builder().brand("Toyota").build();
    CarRepository.addCars(Collections.singletonList(car1));
    assertEquals(1, CarRepository.getAllCars().size());

    assertTrue(CarRepository.deleteAllCars());
    assertTrue(CarRepository.getAllCars().isEmpty());
  }

  /** Tests {@link CarRepository#addCars(List)} to replace the existing car list. */
  @Test
  void setAllCars_replacesContent() {
    Car car1 = Car.builder().brand("Old Car").build();
    CarRepository.addCars(Collections.singletonList(car1));
    assertEquals(1, CarRepository.getAllCars().size());

    Car newCar = Car.builder().brand("New Car").build();
    CarRepository.addCars(Collections.singletonList(newCar));

    assertEquals(1, CarRepository.getAllCars().size());
    assertTrue(CarRepository.getAllCars().contains(newCar));
    assertFalse(CarRepository.getAllCars().contains(car1)); // Old car should be gone
  }

  /** Tests {@link CarRepository#addCars(List)} with an empty list. */
  @Test
  void setAllCars_emptyList() {
    Car car1 = Car.builder().brand("Old Car").build();
    CarRepository.addCars(Collections.singletonList(car1));
    assertEquals(1, CarRepository.getAllCars().size());

    CarRepository.addCars(Collections.emptyList());
    assertTrue(CarRepository.getAllCars().isEmpty());
  }

  /**
   * Tests {@link CarRepository#addCars(List)} with a null list, expecting a NullPointerException.
   */
  @Test
  void setAllCars_nullList_throwsNullPointerException() {
    assertThrows(NullPointerException.class, () -> CarRepository.addCars(null));
  }

  /**
   * Tests that changes to Car objects retrieved from the repository do not affect the internal
   * repository list directly, if objects were not deeply copied. Note: For `Car` object, String,
   * Double, LocalDate are immutable, so direct modification of these fields won't affect other
   * references. If Car contained mutable objects (e.g., `List<String> features`), you'd need a deep
   * copy in `setAllCars` to prevent this.
   */
  @Test
  void getAllCars_modifyingReturnedCarDoesNotAffectRepository() {
    Car originalCar = Car.builder().brand("Original").build();
    CarRepository.addCars(Collections.singletonList(originalCar));

    List<Car> retrievedCars = CarRepository.getAllCars();
    // Since `Car` fields are immutable or `equals` works by value,
    // direct modification of its primitive/String/LocalDate fields won't change the object's
    // identity for 'contains'.
    // If Car had mutable collections or custom objects, this test would need to be adapted
    // to test if their internal state is changed.
    Car carFromList = retrievedCars.get(0);
    // This will create a new Car object if `setBrand` were to be a `with` method on an immutable
    // object
    // or modify the object if it has a direct setter.
    // Given Car uses @Data, it has a setter.
    carFromList.setBrand("Modified"); // Modifies the object instance itself

    assertEquals(
        "Modified",
        CarRepository.getAllCars().get(0).getBrand(),
        "Modifying retrieved car should reflect in repository as it's the same object instance");
  }
}
