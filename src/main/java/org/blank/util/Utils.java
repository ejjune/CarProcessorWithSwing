package org.blank.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.blank.factory.Factory;
import org.blank.repository.model.Car;

public class Utils {

  private Utils() {}

  public static final Map<String, Comparator<Car>> CAR_COMPARATORS =
      Map.of(
          "Brand",
              Comparator.comparing(
                  Car::getBrand, Comparator.nullsLast(String::compareToIgnoreCase)),
          "Type",
              Comparator.comparing(Car::getType, Comparator.nullsLast(String::compareToIgnoreCase)),
          "Model",
              Comparator.comparing(
                  Car::getModel, Comparator.nullsLast(String::compareToIgnoreCase)),
          "Price", Comparator.comparing(Car::getPrice, Comparator.nullsLast(Double::compareTo)),
          "Main Currency",
              Comparator.comparing(
                  Car::getCurrency, Comparator.nullsLast(String::compareToIgnoreCase)),
          "Release Date",
              Comparator.comparing(
                  Car::getReleaseDate, Comparator.nullsLast(LocalDate::compareTo)));

  public static LocalDate parseDateStringSafely(String dateString) {

    DateTimeFormatter dateFormatter =
        new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy,dd,mm"))
            .toFormatter();

    if (StringUtils.isBlank(dateString)) { // Simple isBlank check
      return null;
    }

    try {
      return LocalDate.parse(dateString.trim(), dateFormatter);
    } catch (DateTimeParseException e) {
      displayText("Could not parse date '" + dateString + "'. Invalid format. " + e.getMessage());
      return null;
    }
  }

  public static void displayCars(List<Car> cars) {
    StringBuilder sb = new StringBuilder();
    for (Car car : cars) {
      sb.append(car).append("\n");
    }
    Factory.getResultArea().setText(sb.toString());
  }

  public static void displayText(String message) {
    Factory.getResultArea().setText(message);
  }
}
