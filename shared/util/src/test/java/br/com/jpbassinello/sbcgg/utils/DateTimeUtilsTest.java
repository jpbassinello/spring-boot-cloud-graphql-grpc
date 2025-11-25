package br.com.jpbassinello.sbcgg.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class DateTimeUtilsTest {

  @Test
  void parse() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    var zonedDateTime = DateTimeUtils.parse("2009-11-10");
    assertThat(zonedDateTime).isNotNull();
    assertThat(zonedDateTime.getYear()).isEqualTo(2009);
    assertThat(zonedDateTime.getMonthValue()).isEqualTo(11);
    assertThat(zonedDateTime.getDayOfMonth()).isEqualTo(10);
    assertThat(zonedDateTime.getHour()).isZero();
    assertThat(zonedDateTime.getMinute()).isZero();
    assertThat(zonedDateTime.getSecond()).isZero();
    assertThat(zonedDateTime.getZone()).isEqualTo(ZoneId.systemDefault());
  }

  @Test
  void format() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    var zdt = ZonedDateTime.of(2021, 3, 30, 15, 18, 55, 3339, ZoneId.systemDefault());
    assertThat(DateTimeUtils.format(zdt)).isEqualTo("2021-03-30T15:18:55.000003339Z[UTC]");
  }

  @Test
  void toZonedDateTime() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    assertThat(DateTimeUtils.toZonedDateTime(LocalDate.now()))
        .isEqualTo(LocalDate.now() + "T00:00:00.000000000Z[UTC]");
  }

  @Test
  void isFutureDate() {
    // Test with null date
    assertThat(DateTimeUtils.isFutureDate(null)).isFalse();

    // Test with past date
    ZonedDateTime pastDate = ZonedDateTime.now().minusDays(1);
    assertThat(DateTimeUtils.isFutureDate(pastDate)).isFalse();

    // Test with future date
    ZonedDateTime futureDate = ZonedDateTime.now().plusDays(1);
    assertThat(DateTimeUtils.isFutureDate(futureDate)).isTrue();
  }
}
