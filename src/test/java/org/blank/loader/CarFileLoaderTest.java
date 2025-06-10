package org.blank.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.blank.repository.CarRepository;
import org.blank.repository.model.Car;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CarFileLoaderTest {

  @TempDir Path tempDir; // JUnit 5 provides a temporary directory for file tests

  private File tempCsvFile;
  private File tempXmlFile;

  // Mock Logger to check if warnings/errors are logged

  @BeforeEach
  void setUp() throws IOException {
    // Clear repository before each test
    CarRepository.deleteAllCars();

    // Create temporary files for testing
    tempCsvFile = Files.createFile(tempDir.resolve("cars.csv")).toFile();
    tempXmlFile = Files.createFile(tempDir.resolve("cars.xml")).toFile();
  }

  /**
   * Tests {@link CarFileLoader#process(File, File)} with valid files. Verifies that cars are loaded
   * and added to the repository.
   */
  @Test
  void process_validFiles_loadsAndMergesCars() throws IOException {
    String csvContent = "Brand,ReleaseDate\nToyota,2023-01-01\nHonda,2024-02-15";
    String xmlContent =
        "<cars><car><type>Sedan</type><model>Camry</model><price currency=\"USD\">25000.00</price><prices><price currency=\"EUR\">22000</price></prices></car><car><type>SUV</type><model>CRV</model><price currency=\"USD\">30000</price><prices><price currency=\"EUR\">27000</price></prices></car></cars>";

    try (FileWriter csvWriter = new FileWriter(tempCsvFile);
        FileWriter xmlWriter = new FileWriter(tempXmlFile)) {
      csvWriter.write(csvContent);
      xmlWriter.write(xmlContent);
    }

    CarFileLoader.process(tempCsvFile, tempXmlFile);

    List<Car> cars = CarRepository.getAllCars();
    assertFalse(cars.isEmpty());
    assertEquals(2, cars.size());

    assertEquals("Toyota", cars.get(0).getBrand());
    assertEquals("Sedan", cars.get(0).getType());
    assertEquals(LocalDate.of(2023, 1, 1), cars.get(0).getReleaseDate());
    assertEquals(25000.0, cars.get(0).getPrice());
    assertTrue(cars.get(0).getAdditionalPrices().containsKey("EUR"));

    assertEquals("Honda", cars.get(1).getBrand());
    assertEquals("SUV", cars.get(1).getType());
    assertEquals(LocalDate.of(2024, 2, 15), cars.get(1).getReleaseDate());
    assertEquals(30000.0, cars.get(1).getPrice());
    assertTrue(cars.get(1).getAdditionalPrices().containsKey("EUR"));
  }

  /**
   * Tests {@link CarFileLoader#process(File, File)} with a null CSV file, expecting IOException.
   */
  @Test
  void process_nullCsvFile_throwsIOException() {
    IOException thrown =
        assertThrows(IOException.class, () -> CarFileLoader.process(null, tempXmlFile));
    assertTrue(thrown.getMessage().contains("File is null"));
  }

  /**
   * Tests {@link CarFileLoader#process(File, File)} with a null XML file, expecting IOException.
   */
  @Test
  void process_nullXmlFile_throwsIOException() {
    IOException thrown =
        assertThrows(IOException.class, () -> CarFileLoader.process(tempCsvFile, null));
    assertTrue(thrown.getMessage().contains("File is null"));
  }

  /**
   * Tests {@link CarFileLoader#loadCsv(File)} with a CSV containing malformed lines. Verifies that
   * malformed lines are skipped.
   */
  @Test
  void loadCsv_malformedLines_skipsThem() throws IOException {
    String csvContent =
        """
             Brand,ReleaseDate
             Toyota,2023-01-01
             MalformedLine
             // This line will be skipped
            Honda,2024-02-15""";
    try (FileWriter writer = new FileWriter(tempCsvFile)) {
      writer.write(csvContent);
    }

    List<Car> cars = CarFileLoader.loadCsv(tempCsvFile);
    assertEquals(2, cars.size()); // Only 2 valid cars should be loaded
    // This test would ideally mock the logger to verify a warning was issued.
  }

  /** Tests {@link CarFileLoader#loadCsv(File)} with an empty CSV file. */
  @Test
  void loadCsv_emptyFile_returnsEmptyList() {
    // File is created empty by default in @BeforeEach
    List<Car> cars = CarFileLoader.loadCsv(tempCsvFile);
    assertTrue(cars.isEmpty());
  }

  // --- Tests for loadXml() ---

  /** Tests {@link CarFileLoader#loadXml(File)} with a valid XML file. */
  @Test
  void loadXml_validFile_returnsCorrectCars() throws IOException {
    String xmlContent =
        "<cars>"
            + "<car><type>Sedan</type><model>Accord</model><price currency=\"USD\">35000</price>"
            + "<prices><price currency=\"JPY\">3800000</price><price currency=\"CAD\">45000</price></prices>"
            + "</car>"
            + "<car><type>Hatchback</type><model>Golf</model><price currency=\"EUR\">28000</price>"
            + "<prices><price currency=\"GBP\">25000</price></prices>"
            + "</car>"
            + "</cars>";
    try (FileWriter writer = new FileWriter(tempXmlFile)) {
      writer.write(xmlContent);
    }

    List<Car> cars = CarFileLoader.loadXml(tempXmlFile);
    assertFalse(cars.isEmpty());
    assertEquals(2, cars.size());

    assertEquals("Sedan", cars.get(0).getType());
    assertEquals("Accord", cars.get(0).getModel());
    assertEquals(35000.0, cars.get(0).getPrice());
    assertEquals("USD", cars.get(0).getCurrency());
    assertNotNull(cars.get(0).getAdditionalPrices());
    assertEquals(2, cars.get(0).getAdditionalPrices().size());
    assertEquals(3800000.0, cars.get(0).getAdditionalPrices().get("JPY"));
    assertEquals(45000.0, cars.get(0).getAdditionalPrices().get("CAD"));

    assertEquals("Hatchback", cars.get(1).getType());
    assertEquals("Golf", cars.get(1).getModel());
    assertEquals(28000.0, cars.get(1).getPrice());
    assertEquals("EUR", cars.get(1).getCurrency());
    assertNotNull(cars.get(1).getAdditionalPrices());
    assertEquals(1, cars.get(1).getAdditionalPrices().size());
    assertEquals(25000.0, cars.get(1).getAdditionalPrices().get("GBP"));
  }

  /**
   * Tests {@link CarFileLoader#mergeCars(List, List)} with complete lists. Verifies that fields are
   * merged correctly, preferring CSV values where available.
   */
  @Test
  void mergeCars_completeLists_mergesCorrectly() {
    Car csvCar1 =
        Car.builder()
            .brand("CSVBrand1")
            .releaseDate(LocalDate.of(2020, 1, 1))
            .additionalPrices(Map.of("USD", 100d))
            .build();
    Car csvCar2 =
        Car.builder()
            .brand("CSVBrand2")
            .releaseDate(LocalDate.of(2021, 1, 1))
            .build(); // No additionalPrice

    Car xmlCar1 =
        Car.builder()
            .type("XMLType1")
            .model("XMLModel1")
            .price(1000.0)
            .currency("XMLCurr1")
            .additionalPrices(Map.of("A", 100.0, "B", 200.0))
            .build();
    Car xmlCar2 =
        Car.builder()
            .type("XMLType2")
            .model("XMLModel2")
            .price(2000.0)
            .currency("XMLCurr2")
            .additionalPrices(Map.of("C", 300.0))
            .build();

    List<Car> csvCars = Arrays.asList(csvCar1, csvCar2);
    List<Car> xmlCars = Arrays.asList(xmlCar1, xmlCar2);

    List<Car> merged = CarFileLoader.mergeCars(csvCars, xmlCars);
    assertEquals(2, merged.size());

    // Verify mergedCar1 (CSV values prioritized)
    assertEquals("CSVBrand1", merged.get(0).getBrand());
    assertEquals("XMLType1", merged.get(0).getType()); // Type only in XML
    assertEquals("XMLModel1", merged.get(0).getModel()); // Model only in XML
    assertEquals(
        1000.0,
        merged.get(0).getPrice()); // Price only in XML (primitive double default 0.0 for csvCar1)
    assertEquals("XMLCurr1", merged.get(0).getCurrency()); // Currency only in XML
    assertEquals(
        LocalDate.of(2020, 1, 1), merged.get(0).getReleaseDate()); // ReleaseDate only in CSV
    assertNotNull(merged.get(0).getAdditionalPrices());
    assertEquals(3, merged.get(0).getAdditionalPrices().size()); // 1 from CSV, 2 from XML, merged
    assertEquals(100.0, merged.get(0).getAdditionalPrices().get("A"));
    assertEquals(200.0, merged.get(0).getAdditionalPrices().get("B"));

    // Verify mergedCar2 (CSV values prioritized)
    assertEquals("CSVBrand2", merged.get(1).getBrand());
    assertEquals("XMLType2", merged.get(1).getType());
    assertEquals(LocalDate.of(2021, 1, 1), merged.get(1).getReleaseDate());
    assertNotNull(merged.get(1).getAdditionalPrices());
    assertEquals(1, merged.get(1).getAdditionalPrices().size());
    assertEquals(300.0, merged.get(1).getAdditionalPrices().get("C"));
  }

  /**
   * Tests {@link CarFileLoader#mergeCars(List, List)} when CSV list is shorter. Verifies that
   * merging stops at the size of the smaller list.
   */
  @Test
  void mergeCars_csvShorter_returnNull() {
    Car csvCar1 = Car.builder().brand("CSVBrand1").build();
    Car xmlCar1 = Car.builder().type("XMLType1").build();
    Car xmlCar2 = Car.builder().type("XMLType2").build(); // This car won't be merged

    List<Car> csvCars = Collections.singletonList(csvCar1);
    List<Car> xmlCars = Arrays.asList(xmlCar1, xmlCar2);

    List<Car> merged = CarFileLoader.mergeCars(csvCars, xmlCars);
    assertNull(merged);
  }

  /**
   * Tests {@link CarFileLoader#mergeCars(List, List)} when XML list is shorter. Verifies that
   * merging stops at the size of the smaller list.
   */
  @Test
  void mergeCars_xmlShorter_returnNull() {
    Car csvCar1 = Car.builder().brand("CSVBrand1").build();
    Car csvCar2 = Car.builder().brand("CSVBrand2").build(); // This car won't be merged
    Car xmlCar1 = Car.builder().type("XMLType1").build();

    List<Car> csvCars = Arrays.asList(csvCar1, csvCar2);
    List<Car> xmlCars = Collections.singletonList(xmlCar1);

    List<Car> merged = CarFileLoader.mergeCars(csvCars, xmlCars);
    assertNull(merged);
  }

  /** Tests {@link CarFileLoader#mergeCars(List, List)} with empty lists. */
  @Test
  void mergeCars_emptyLists_returnsEmptyList() {
    List<Car> merged = CarFileLoader.mergeCars(Collections.emptyList(), Collections.emptyList());
    assertTrue(merged.isEmpty());
  }

  /** Tests `mergeAdditionalPrices` helper method. */
  @Test
  void mergeAdditionalPrices_mergesCorrectly() {
    Map<String, Double> csvPrices = new HashMap<>();
    csvPrices.put("USD", 100.0);
    csvPrices.put("EUR", 90.0);

    Map<String, Double> xmlPrices = new HashMap<>();
    xmlPrices.put("EUR", 95.0); // Duplicate, CSV preferred
    xmlPrices.put("GBP", 80.0);

    List<Car> csvCars =
        Collections.singletonList(Car.builder().brand("test").additionalPrices(csvPrices).build());
    List<Car> xmlCars =
        Collections.singletonList(Car.builder().brand("test").additionalPrices(xmlPrices).build());

    List<Car> merged = CarFileLoader.mergeCars(csvCars, xmlCars);
    Map<String, Double> resultPrices = merged.get(0).getAdditionalPrices();

    assertNotNull(resultPrices);
    assertEquals(3, resultPrices.size());
    assertEquals(100.0, resultPrices.get("USD")); // Only from CSV
    assertEquals(90.0, resultPrices.get("EUR")); // CSV preferred
    assertEquals(80.0, resultPrices.get("GBP")); // Only from XML
  }

  @Test
  void mergeAdditionalPrices_oneMapNull() {
    Map<String, Double> prices = new HashMap<>();
    prices.put("USD", 100.0);

    List<Car> csvCars =
        Collections.singletonList(Car.builder().brand("test").additionalPrices(prices).build());
    List<Car> xmlCarsNull =
        Collections.singletonList(Car.builder().brand("test").additionalPrices(null).build());

    List<Car> merged1 = CarFileLoader.mergeCars(csvCars, xmlCarsNull);
    assertEquals(1, merged1.get(0).getAdditionalPrices().size());
    assertEquals(100.0, merged1.get(0).getAdditionalPrices().get("USD"));

    List<Car> csvCarsNull =
        Collections.singletonList(Car.builder().brand("test").additionalPrices(null).build());
    List<Car> xmlCars =
        Collections.singletonList(Car.builder().brand("test").additionalPrices(prices).build());

    List<Car> merged2 = CarFileLoader.mergeCars(csvCarsNull, xmlCars);
    assertEquals(1, merged2.get(0).getAdditionalPrices().size());
    assertEquals(100.0, merged2.get(0).getAdditionalPrices().get("USD"));
  }
}
