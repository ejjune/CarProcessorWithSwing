package org.blank.factory;

import javax.swing.JTextArea;

public class Factory {

  private Factory() {}

  private static JTextArea resultArea;

  static {
    resultArea = new JTextArea(10, 60);
    resultArea.setEditable(false);
  }

  public static JTextArea getResultArea() {
    return resultArea;
  }
}
