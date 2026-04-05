package fr.bordeaux.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TimeConvertorTest {

  @Test
  public void checkFormatTime() {
    assertTrue(TimeConvertor.formatTime(0).equals("00 : 00"));
    assertTrue(TimeConvertor.formatTime(1528).equals("01 : 52"));
    assertTrue(TimeConvertor.formatTime(65524).equals("01 : 05"));
  }
}
