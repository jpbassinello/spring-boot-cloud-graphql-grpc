package br.com.jpbassinello.sbcgg.mapstruct;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BaseMapperTest {

  static List<Arguments> bigDecimalScaleArgs() {
    return List.of(
        Arguments.of(1, new BigDecimal("1.00")),
        Arguments.of(1.0, new BigDecimal("1.00")),
        Arguments.of(1.00, new BigDecimal("1.00")),
        Arguments.of(1.000, new BigDecimal("1.00")),
        Arguments.of(1.001, new BigDecimal("1.001")),
        Arguments.of(1.07, new BigDecimal("1.07")),
        Arguments.of(1.76, new BigDecimal("1.76")),
        Arguments.of(1.075, new BigDecimal("1.075")),
        Arguments.of(19.07563, new BigDecimal("19.07563"))
    );
  }

  @Test
  void testStringLocalDate() {
    assertThat(BaseMapper.mapStringToLocalDate("")).isNull();
    assertThat(BaseMapper.mapStringToLocalDate(null)).isNull();
    assertThat(BaseMapper.mapStringToLocalDate("2021-05-01")).isEqualTo(LocalDate.parse("2021-05-01"));

    assertThat(BaseMapper.mapLocalDateToString(null)).isNull();
    assertThat(BaseMapper.mapLocalDateToString(LocalDate.parse("2021-05-01"))).isEqualTo("2021-05-01");
  }

  @Test
  void testUUIDString() {
    var uuid = UUID.randomUUID();
    assertThat(BaseMapper.mapUUIDToString(null)).isNull();
    assertThat(BaseMapper.mapUUIDToString(uuid)).isEqualTo(uuid.toString());

    assertThat(BaseMapper.mapStringToUUID(null)).isNull();
    assertThat(BaseMapper.mapStringToUUID(uuid.toString())).isEqualTo(uuid);
  }

  @Test
  void testStringZonedDateTime() {
    assertThat(BaseMapper.mapStringToZonedDateTime("")).isNull();
    assertThat(BaseMapper.mapStringToZonedDateTime(null)).isNull();
    assertThat(BaseMapper.mapStringToZonedDateTime("2021-05-01T10:01:01Z"))
        .isEqualTo(ZonedDateTime.parse("2021-05-01T10:01:01Z"));
    assertThat(BaseMapper.mapZonedDateTimeToString(null)).isNull();
    assertThat(BaseMapper.mapZonedDateTimeToString(ZonedDateTime.parse("2021-05-01T10:01:01Z")))
        .isEqualTo("2021-05-01T10:01:01Z");
  }

  @Test
  void testBigDecimalDouble() {
    assertThat(BaseMapper.mapBigDecimalToDouble(null)).isNull();
    assertThat(BaseMapper.mapBigDecimalToDouble(new BigDecimal("3.15"))).isEqualTo(3.15D);
  }

  @Test
  void testBooleanInteger() {
    assertThat(BaseMapper.mapBooleanToInteger(true)).isEqualTo(1);
    assertThat(BaseMapper.mapBooleanToInteger(false)).isZero();
  }

  @Test
  void testURLtoString() throws MalformedURLException {
    assertThat(BaseMapper.map(null)).isNull();
    assertThat(BaseMapper.map(new URL("https://www.possiblefinance.com")))
        .isEqualTo("https://www.possiblefinance.com");
  }

  @ParameterizedTest
  @MethodSource("bigDecimalScaleArgs")
  void shouldProperlyScaleBigDecimalResults(double input, BigDecimal expected) {
    var actual = BaseMapper.mapDoubleToBigDecimal(input);

    // should validate number and scale
    assertThat(actual).isEqualTo(expected);
  }
}