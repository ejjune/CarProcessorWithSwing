package org.blank.factory;

import javax.swing.JTextArea;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

class FactoryTest {

  /** Tests that {@link Factory#getResultArea()} returns a non-null JTextArea instance. */
  @Test
  void getResultArea_returnsNotNull() {
    JTextArea resultArea = Factory.getResultArea();
    assertNotNull(resultArea, "getResultArea should return a non-null JTextArea");
  }

  /**
   * Tests that {@link Factory#getResultArea()} always returns the same instance (singleton
   * pattern).
   */
  @Test
  void getResultArea_returnsSameInstance() {
    JTextArea resultArea1 = Factory.getResultArea();
    JTextArea resultArea2 = Factory.getResultArea();
    assertSame(
        resultArea1,
        resultArea2,
        "getResultArea should return the same JTextArea instance every time");
  }

  /** Tests that the JTextArea returned by {@link Factory#getResultArea()} is not editable. */
  @Test
  void getResultArea_isNotEditable() {
    JTextArea resultArea = Factory.getResultArea();
    assertFalse(resultArea.isEditable(), "The JTextArea should not be editable");
  }
}
