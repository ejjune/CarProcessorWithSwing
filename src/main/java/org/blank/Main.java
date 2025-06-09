package org.blank;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import org.blank.factory.Factory;
import org.blank.service.CarService;

public class Main {

  public static void main(String[] args) {
    JFrame frame = new JFrame("Car File Processor");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setSize(800, 400);

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5); // Padding

    JButton loadXml = new JButton("Load XML");
    JButton loadCsv = new JButton("Load CSV");
    JButton processFileButton = new JButton("Process File");
    JButton processWithFiltersButton = new JButton("Process with Filters"); // Stays in filterPanel
    JButton filterButton = new JButton("Filter");
    JButton sortButton = new JButton("Sort By"); // This button toggles visibility of sortPanel

    JTextField xmlPathField = new JTextField(40);
    JTextField csvPathField = new JTextField(40);

    // Filter fields (Initially hidden)
    JPanel filterPanel = new JPanel();
    filterPanel.setLayout(new GridLayout(8, 2)); // 7 label/textfield rows + 1 row for the button
    filterPanel.setVisible(false); // Hide filter panel initially

    JTextField brandFilter = new JTextField();
    JTextField typeFilter = new JTextField();
    JTextField modelFilter = new JTextField();
    JTextField priceFilter = new JTextField();
    JTextField currencyFilter = new JTextField();
    JTextField releaseDateFilter = new JTextField();

    filterPanel.add(new JLabel("Brand:"));
    filterPanel.add(brandFilter);
    filterPanel.add(new JLabel("Type:"));
    filterPanel.add(typeFilter);
    filterPanel.add(new JLabel("Model:"));
    filterPanel.add(modelFilter);
    filterPanel.add(new JLabel("Price:"));
    filterPanel.add(priceFilter);
    filterPanel.add(new JLabel("Main Currency:"));
    filterPanel.add(currencyFilter);
    filterPanel.add(new JLabel("Release Date (yyyy-MM-dd):"));
    filterPanel.add(releaseDateFilter);

    filterPanel.add(new JLabel("")); // Placeholder for left column of button row
    filterPanel.add(processWithFiltersButton); // Placed inside filterPanel

    // Sort fields (Initially hidden)
    JPanel sortPanel = new JPanel();
    sortPanel.setLayout(
        new GridBagLayout()); // Use GridBagLayout for sortPanel for better control over button
    // placement
    GridBagConstraints sortGbc = new GridBagConstraints();
    sortGbc.insets = new Insets(2, 5, 2, 5); // Smaller padding for sort elements
    sortPanel.setVisible(false); // Hide sort panel initially

    String[] sortFields = {
      "None", "Brand", "Type", "Model", "Price", "Main Currency", "Release Date"
    };
    JComboBox<String> sortFieldComboBox = new JComboBox<>(sortFields);

    String[] sortOrders = {"Ascending", "Descending"};
    JComboBox<String> sortOrderComboBox = new JComboBox<>(sortOrders);

    JButton applySortButton = new JButton("Apply Sort"); // Button to trigger the sort

    // Add components to sortPanel using sortGbc
    sortGbc.gridx = 0;
    sortGbc.gridy = 0;
    sortGbc.anchor = GridBagConstraints.WEST;
    sortPanel.add(new JLabel("Sort Field:"), sortGbc);

    sortGbc.gridx = 1;
    sortGbc.fill = GridBagConstraints.HORIZONTAL;
    sortGbc.weightx = 0.5;
    sortPanel.add(sortFieldComboBox, sortGbc);

    sortGbc.gridx = 2;
    sortGbc.gridy = 0;
    sortGbc.anchor = GridBagConstraints.WEST;
    sortGbc.fill = GridBagConstraints.NONE;
    sortGbc.weightx = 0;
    sortPanel.add(new JLabel("Order:"), sortGbc);

    sortGbc.gridx = 3;
    sortGbc.fill = GridBagConstraints.HORIZONTAL;
    sortGbc.weightx = 0.5;
    sortPanel.add(sortOrderComboBox, sortGbc);

    sortGbc.gridx = 4;
    sortGbc.gridy = 0;
    sortGbc.gridwidth = 1;
    sortGbc.anchor = GridBagConstraints.EAST;
    sortGbc.fill = GridBagConstraints.NONE;
    sortGbc.weightx = 0;
    sortPanel.add(applySortButton, sortGbc);

    // --- Layout of mainPanel Components ---

    // XML row (gridy=0)
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;
    mainPanel.add(loadXml, gbc);

    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.WEST;
    mainPanel.add(new JLabel("XML File Path:"), gbc);

    gbc.gridx = 2;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    mainPanel.add(xmlPathField, gbc);

    // CSV row (gridy=1)
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;
    mainPanel.add(loadCsv, gbc);

    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.WEST;
    mainPanel.add(new JLabel("CSV File Path:"), gbc);

    gbc.gridx = 2;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    mainPanel.add(csvPathField, gbc);

    // Process File button (gridy=2)
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 3; // Spans across the full width
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;
    mainPanel.add(processFileButton, gbc);

    // Filter button (gridy=3)
    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.gridwidth = 3; // Spans across the full width for consistency with sort button below
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;
    mainPanel.add(filterButton, gbc);

    // Filter panel (gridy=4, below filter button)
    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.gridwidth = 3;
    mainPanel.add(filterPanel, gbc);

    // Sort By button (gridy=5, below filter panel)
    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.gridwidth = 3; // Spans across the full width
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;
    mainPanel.add(sortButton, gbc);

    // Sort panel (gridy=6, below Sort By button)
    gbc.gridx = 0;
    gbc.gridy = 6;
    gbc.gridwidth = 3;
    mainPanel.add(sortPanel, gbc);

    // Result area (gridy=7, shifted down)
    gbc.gridx = 0;
    gbc.gridy = 7;
    gbc.gridwidth = 3;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    // Assuming Factory.getResultArea() correctly provides the JTextArea instance
    mainPanel.add(new JScrollPane(Factory.getResultArea()), gbc);

    frame.add(mainPanel);
    frame.setVisible(true);
    frame.pack();

    // Action Listeners for buttons
    loadCsv.addActionListener(e -> CarService.loadCsvFile(csvPathField));
    loadXml.addActionListener(e -> CarService.loadXmlFile(xmlPathField));
    processFileButton.addActionListener(
        e -> CarService.process()); // Call without resultArea if CarService manages it

    processWithFiltersButton.addActionListener(
        e ->
            CarService.processWithFilters(
                brandFilter.getText(),
                typeFilter.getText(),
                modelFilter.getText(),
                priceFilter.getText(),
                currencyFilter.getText(),
                releaseDateFilter.getText())); // Added additionalPriceFilter.getText()

    filterButton.addActionListener(
        e -> {
          filterPanel.setVisible(!filterPanel.isVisible());
          frame.pack();
        });

    sortButton.addActionListener(
        e -> {
          sortPanel.setVisible(!sortPanel.isVisible()); // Toggle visibility of sort controls
          frame.pack();
        });

    applySortButton.addActionListener(
        e -> {
          String selectedField = (String) sortFieldComboBox.getSelectedItem();
          String selectedOrder = (String) sortOrderComboBox.getSelectedItem();
          CarService.sortAndDisplay(
              selectedField, selectedOrder); // Call without resultArea if CarService manages it
        });
  }
}
