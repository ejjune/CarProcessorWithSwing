package org.blank.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;
import org.blank.loader.CarFileLoader;
import org.blank.repository.CarRepository;
import org.blank.repository.model.Car;
import org.blank.util.Utils;

public class CarService {

  private CarService() {}

  private static File csvFile;

  private static File xmlFile;

  public static void loadCsvFile(JTextField textArea) {
    try {
      File file = chooseFile("CSV");
      if (file != null) {
        textArea.setText(file.getAbsolutePath());
        csvFile = file;
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "Error loading CSV file: " + e.getMessage());
    }
  }

  public static void loadXmlFile(JTextField textArea) {
    try {
      File file = chooseFile("XML");
      if (file != null) {
        textArea.setText(file.getAbsolutePath());
        xmlFile = file;
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "Error loading XML file: " + e.getMessage());
    }
  }

  public static void process() {
    try {
      CarFileLoader.process(csvFile, xmlFile);
      Utils.displayCars(CarRepository.getAllCars());
    } catch (IOException e) {
      Utils.displayText(e.getMessage());
    }
  }

  public static void sortAndDisplay(String sortField, String sortOrder) {
    if (CarRepository.getAllCars().isEmpty()) {
      Utils.displayText("No cars to sort. Please load data first.");
      return;
    }

    if ("None".equals(sortField)) {
      Utils.displayCars(CarRepository.getAllCars());
      return;
    }

    Comparator<Car> comparator = Utils.CAR_COMPARATORS.get(sortField);

    if ("Descending".equals(sortOrder)) {
      comparator = comparator.reversed();
    }

    // Sort the loadedCars list in place
    ArrayList<Car> sortedList =
        CarRepository.getAllCars().stream()
            .sorted(comparator)
            .collect(Collectors.toCollection(ArrayList::new));

    Utils.displayCars(sortedList); // Update the UI with sorted results
  }

  public static void processWithFilters(
      String brand,
      String type,
      String model,
      String price,
      String currency,
      String releaseDateString) {

    Double priceFilter = StringUtils.isNumeric(price) ? Double.parseDouble(price.trim()) : null;

    LocalDate releaseDateFilter = Utils.parseDateStringSafely(releaseDateString);

    List<Car> carsToFilter = CarRepository.getAllCars();

    List<Car> filteredCars =
        carsToFilter.stream()
            .filter(
                e ->
                    StringUtils.isBlank(brand)
                        || (Objects.nonNull(e.getBrand())
                            && e.getBrand().equalsIgnoreCase(brand.trim())))
            .filter(
                e ->
                    StringUtils.isBlank(type)
                        || (Objects.nonNull(e.getType())
                            && e.getType().equalsIgnoreCase(type.trim())))
            .filter(
                e ->
                    StringUtils.isBlank(model)
                        || (Objects.nonNull(e.getModel())
                            && e.getModel().equalsIgnoreCase(model.trim())))
            .filter(
                e ->
                    Objects.isNull(priceFilter)
                        || (Objects.nonNull(e.getPrice())
                            && Objects.equals(e.getPrice(), priceFilter)))
            .filter(
                e ->
                    StringUtils.isBlank(currency)
                        || (Objects.nonNull(e.getCurrency())
                            && e.getCurrency().equalsIgnoreCase(currency.trim())))
            .filter(
                e ->
                    Objects.isNull(releaseDateFilter)
                        || (Objects.nonNull(e.getReleaseDate())
                            && e.getReleaseDate().isEqual(releaseDateFilter)))
            .collect(Collectors.toCollection(ArrayList::new));

    Utils.displayCars(filteredCars);
  }

  private static File chooseFile(String fileType) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Select " + fileType + " file");

    if ("CSV".equals(fileType)) {
      fileChooser.setFileFilter(
          new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
    } else if ("XML".equals(fileType)) {
      fileChooser.setFileFilter(
          new javax.swing.filechooser.FileNameExtensionFilter("XML Files", "xml"));
    }

    int result = fileChooser.showOpenDialog(null);
    if (result == JFileChooser.APPROVE_OPTION) {
      return fileChooser.getSelectedFile();
    }
    return null;
  }
}
