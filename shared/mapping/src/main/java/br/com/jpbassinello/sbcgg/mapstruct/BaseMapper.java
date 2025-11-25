package br.com.jpbassinello.sbcgg.mapstruct;

import br.com.jpbassinello.sbcgg.utils.DateTimeUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class BaseMapper {

  public static LocalDate mapStringToLocalDate(String value) {
    return StringUtils.isBlank(value) ? null : LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
  }

  public static String mapLocalDateToString(LocalDate value) {
    return value == null ? null : value.format(DateTimeFormatter.ISO_LOCAL_DATE);
  }

  public static String mapUUIDToString(UUID value) {
    return value == null ? null : value.toString();
  }

  public static UUID mapStringToUUID(String value) {
    return StringUtils.isBlank(value) ? null : UUID.fromString(value);
  }

  public static String mapZonedDateTimeToString(ZonedDateTime value) {
    return value == null ? null : DateTimeUtils.format(value);
  }

  public static ZonedDateTime mapStringToZonedDateTime(String value) {
    return StringUtils.isBlank(value) ? null : DateTimeUtils.parse(value);
  }

  public static Double mapBigDecimalToDouble(BigDecimal value) {
    return value == null ? null : value.doubleValue();
  }

  /**
   * If converting from an int to BigDecimal, MapStruct will convert to a double first and call this method.
   * In order to get a 0 scale BigDecimal for an int, mapIntToBigDecimal needs to be explicitly called
   */
  public static BigDecimal mapDoubleToBigDecimal(double value) {
    var decimal = BigDecimal.valueOf(value);
    if (decimal.scale() < 2) {
      decimal = decimal.setScale(2, RoundingMode.CEILING);
    }
    return decimal;
  }

  public static BigDecimal mapIntToBigDecimal(int value) {
    return BigDecimal.valueOf(value);
  }

  public static ZonedDateTime mapLocalDateToZonedDateTime(LocalDate value) {
    return value == null ? null : DateTimeUtils.toZonedDateTime(value);
  }

  public static Integer mapBooleanToInteger(boolean value) {
    return value ? 1 : 0;
  }

  public static boolean mapToBooleanPrimitive(Boolean value) {
    return BooleanUtils.toBoolean(value);
  }

  public static String map(URL value) {
    return (value == null) ? null : value.toString();
  }
}
