package org.blank.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.blank.repository.CarRepository;
import org.blank.repository.model.Car;
import org.blank.util.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CarFileLoader {

  static Logger logger = Logger.getLogger(CarFileLoader.class.getName());

  private CarFileLoader() {}

  public static void process(File csvFile, File xmlFile) throws IOException {
    CarRepository.deleteAllCars();

    if (Objects.isNull(csvFile) || Objects.isNull(xmlFile)) {
      throw new IOException("File is null");
    }

    List<Car> carList = mergeCars(loadCsv(csvFile), loadXml(xmlFile));
    CarRepository.addCars(carList);
  }

  public static List<Car> loadCsv(File file) {
    List<Car> cars = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;

      String header = reader.readLine();

      logger.info(header);

      while ((line = reader.readLine()) != null) {
        String[] fields = line.split(",");

        if (fields.length == 2) {
          String brand = fields[0].trim().replace("\"", "");
          String releaseDateStr = fields[1].trim().replace("\"", "");
          LocalDate releaseDate = Utils.parseDateStringSafely(releaseDateStr);

          Car car = Car.builder().brand(brand).releaseDate(releaseDate).build();

          car.setReleaseDate(releaseDate);
          cars.add(car);
        }
      }
    } catch (IOException e) {
      Utils.displayText(e.getMessage());
    }

    return cars;
  }

  public static List<Car> loadXml(File file) {
    List<Car> cars = new ArrayList<>();
    try {
      Document doc;
      try (FileInputStream fileInputStream = new FileInputStream(file)) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbFactory.setFeature(
            "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dbFactory.setXIncludeAware(false);

        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.parse(fileInputStream);
      }
      doc.getDocumentElement().normalize();

      // Get all <car> nodes
      NodeList carNodes = doc.getElementsByTagName("car");

      for (int i = 0; i < carNodes.getLength(); i++) {
        Node carNode = carNodes.item(i);

        if (carNode.getNodeType() == Node.ELEMENT_NODE) {
          Element carElement = (Element) carNode;

          // Get the car type and model
          String type = carElement.getElementsByTagName("type").item(0).getTextContent();
          String model = carElement.getElementsByTagName("model").item(0).getTextContent();

          // Get the main price and currency
          Element priceElement = (Element) carElement.getElementsByTagName("price").item(0);
          double price = Double.parseDouble(priceElement.getTextContent());
          String currency = priceElement.getAttribute("currency");

          // Create a new Car object
          Car car = Car.builder().type(type).model(model).price(price).currency(currency).build();

          // Extract all the prices under the <prices> tag with different currencies
          NodeList priceNodes = carElement.getElementsByTagName("prices").item(0).getChildNodes();

          for (int j = 0; j < priceNodes.getLength(); j++) {
            Node priceNode = priceNodes.item(j);
            if (priceNode.getNodeType() == Node.ELEMENT_NODE) {
              Element priceDetailElement = (Element) priceNode;
              String priceCurrency = priceDetailElement.getAttribute("currency");
              double priceValue = Double.parseDouble(priceDetailElement.getTextContent());

              car.addPrice(priceCurrency, priceValue); // Add price to the car
            }
          }

          cars.add(car);
        }
      }
    } catch (Exception e) {
      Utils.displayText(e.getMessage());
    }
    return cars;
  }

  public static List<Car> mergeCars(List<Car> csvCars, List<Car> xmlCars) {
    List<Car> mergedCars = new ArrayList<>();

    if (csvCars.size() != xmlCars.size()) {
      Utils.displayText("CSV and XML does not have the same size");
      return Collections.emptyList();
    }

    // Assuming csvCars and xmlCars have the same length
    for (int i = 0; i < csvCars.size(); i++) {
      Car csvCar = csvCars.get(i);
      Car xmlCar = xmlCars.get(i);

      // Use the builder pattern to merge the fields
      Car mergedCar =
          Car.builder()
              .brand(csvCar.getBrand() != null ? csvCar.getBrand() : xmlCar.getBrand())
              .type(csvCar.getType() != null ? csvCar.getType() : xmlCar.getType())
              .model(csvCar.getModel() != null ? csvCar.getModel() : xmlCar.getModel())
              .currency(csvCar.getCurrency() != null ? csvCar.getCurrency() : xmlCar.getCurrency())
              .price(csvCar.getPrice() > 0 ? csvCar.getPrice() : xmlCar.getPrice())
              .releaseDate(
                  csvCar.getReleaseDate() != null
                      ? csvCar.getReleaseDate()
                      : xmlCar.getReleaseDate())
              .additionalPrices(
                  mergeAdditionalPrices(csvCar.getAdditionalPrices(), xmlCar.getAdditionalPrices()))
              .build();

      // Add the merged car to the list
      mergedCars.add(mergedCar);
    }

    return mergedCars;
  }

  private static Map<String, Double> mergeAdditionalPrices(
      Map<String, Double> csvPrices, Map<String, Double> xmlPrices) {

    csvPrices = csvPrices == null || csvPrices.isEmpty() ? new HashMap<>() : csvPrices;
    xmlPrices = xmlPrices == null || xmlPrices.isEmpty() ? new HashMap<>() : xmlPrices;

    Map<String, Double> mergedPrices = new HashMap<>(csvPrices);

    // Merge prices from both maps
    for (Map.Entry<String, Double> entry : xmlPrices.entrySet()) {
      mergedPrices.merge(entry.getKey(), entry.getValue(), (v1, v2) -> v1 != null ? v1 : v2);
    }

    return mergedPrices;
  }
}
