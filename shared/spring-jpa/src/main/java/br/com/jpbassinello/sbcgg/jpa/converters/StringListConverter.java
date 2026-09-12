package br.com.jpbassinello.sbcgg.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

  private static final String SEPARATOR = "##";

  @Override
  public String convertToDatabaseColumn(List<String> attribute) {
    if (attribute == null || attribute.isEmpty()) {
      return null;
    }
    var sanitized = attribute.stream()
        .map(StringListConverter::stripNullBytes)
        .toList();
    return String.join(SEPARATOR, sanitized);
  }

  /**
   * Removes NUL (0x00) characters, which PostgreSQL rejects in text columns
   * ("invalid byte sequence for encoding UTF8: 0x00"). Externally sourced content can carry them.
   */
  private static String stripNullBytes(String value) {
    if (value == null || value.indexOf('\0') < 0) {
      return value;
    }
    return value.replace("\0", "");
  }

  @Override
  public List<String> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isEmpty()) {
      return List.of();
    }
    return Arrays.asList(dbData.split(SEPARATOR));
  }
}