package br.com.jpbassinello.sbcgg.jpa.converters;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StringListConverter Tests")
class StringListConverterTest {

  private final StringListConverter converter = new StringListConverter();

  @Test
  @DisplayName("should convert list to database column with separator")
  void shouldConvertListToDbColumn() {
    var result = converter.convertToDatabaseColumn(List.of("a", "b", "c"));
    assertThat(result).isEqualTo("a##b##c");
  }

  @Test
  @DisplayName("should convert single item list to database column")
  void shouldConvertSingleItemList() {
    var result = converter.convertToDatabaseColumn(List.of("only"));
    assertThat(result).isEqualTo("only");
  }

  @Test
  @DisplayName("should convert null list to null column")
  void shouldConvertNullListToNull() {
    assertThat(converter.convertToDatabaseColumn(null)).isNull();
  }

  @Test
  @DisplayName("should convert empty list to null column")
  void shouldConvertEmptyListToNull() {
    assertThat(converter.convertToDatabaseColumn(List.of())).isNull();
  }

  @Test
  @DisplayName("should convert database column to list")
  void shouldConvertDbColumnToList() {
    var result = converter.convertToEntityAttribute("a##b##c");
    assertThat(result).containsExactly("a", "b", "c");
  }

  @Test
  @DisplayName("should convert single value column to list")
  void shouldConvertSingleValueColumn() {
    var result = converter.convertToEntityAttribute("only");
    assertThat(result).containsExactly("only");
  }

  @Test
  @DisplayName("should convert null column to empty list")
  void shouldConvertNullColumnToEmptyList() {
    assertThat(converter.convertToEntityAttribute(null)).isEmpty();
  }

  @Test
  @DisplayName("should convert empty column to empty list")
  void shouldConvertEmptyColumnToEmptyList() {
    assertThat(converter.convertToEntityAttribute("")).isEmpty();
  }

  @Test
  @DisplayName("should round-trip empty list correctly")
  void shouldRoundTripEmptyList() {
    var dbValue = converter.convertToDatabaseColumn(List.of());
    var result = converter.convertToEntityAttribute(dbValue);
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("should round-trip non-empty list correctly")
  void shouldRoundTripNonEmptyList() {
    var original = List.of("evidence 1", "evidence 2");
    var dbValue = converter.convertToDatabaseColumn(original);
    var result = converter.convertToEntityAttribute(dbValue);
    assertThat(result).containsExactlyElementsOf(original);
  }
}
