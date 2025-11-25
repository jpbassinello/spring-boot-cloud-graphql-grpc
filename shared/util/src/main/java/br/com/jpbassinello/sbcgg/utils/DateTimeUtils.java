package br.com.jpbassinello.sbcgg.utils;

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeUtils {

  private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS =
      List.of(
          DateTimeFormatter.ISO_ZONED_DATE_TIME,
          DateTimeFormatter.ISO_OFFSET_DATE_TIME,
          DateTimeFormatter.ISO_LOCAL_DATE_TIME
      );

  private static final List<DateTimeFormatter> DATE_FORMATTERS =
      List.of(
          DateTimeFormatter.ISO_LOCAL_DATE
      );

  @Nullable
  public static ZonedDateTime parse(String dateTime) {
    if (dateTime != null) {
      for (var dateTimeFormatter : DATE_TIME_FORMATTERS) {
        try {
          return ZonedDateTime.parse(dateTime, dateTimeFormatter);
        } catch (DateTimeParseException ignored) {
        }
      }

      for (var dateFormatter : DATE_FORMATTERS) {
        try {
          return LocalDate.parse(dateTime, dateFormatter).atStartOfDay().atZone(ZoneId.systemDefault());
        } catch (DateTimeParseException ignored) {
        }
      }
    }

    return null;
  }

  @Nullable
  public static String format(ZonedDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }

    return dateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
  }

  public static ZonedDateTime toZonedDateTime(LocalDate localDate) {
    return localDate.atStartOfDay(ZoneId.systemDefault());
  }

  /**
   * Checks if a date is in the future.
   *
   * @param date the date to check
   * @return true if the date is in the future, false otherwise
   */
  public static boolean isFutureDate(ZonedDateTime date) {
    if (date == null) {
      return false;
    }
    return date.isAfter(ZonedDateTime.now());
  }
}
