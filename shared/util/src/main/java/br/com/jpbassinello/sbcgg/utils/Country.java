package br.com.jpbassinello.sbcgg.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * ISO 3166-1 alpha-2 country codes recognized by the platform.
 * Unrecognized codes from upstream data (LLM, ATS, free-text metadata) are dropped on read
 * so the application layer always sees a clean list. Add new values here as needed.
 */
public enum Country {
  US,
  BR,
  CA,
  GB,
  DE,
  NL,
  IE,
  IN,
  AU,
  FR,
  NG,
  PK,
  AE,
  KE,
  MX,
  PH,
  PT,
  RO,
  GH,
  AO,
  JM,
  UY,
  PA,
  HN,
  JO,
  LB,
  BD,
  SG,
  GE,
  TR,
  UA,
  ES,
  MU,
  NZ,
  SE,
  DK,
  NO,
  FI,
  IT,
  PL,
  EE,
  LT,
  CZ,
  AT,
  CH,
  BE,
  HU,
  BG,
  IL,
  CO,
  AR,
  CL,
  ID,
  VN,
  MY,
  ZA,
  LK,
  EG,
  BO,
  SV,
  NP,
  ET,
  VE,
  GR,
  AL,
  MK,
  MD,
  SK,
  IS,
  MT,
  DZ,
  TN,
  MA,
  ZW,
  RW,
  CM,
  SL,
  TZ,
  JP,
  BH,
  QA,
  UZ,
  BM;

  @JsonCreator
  @Nullable
  public static Country tryFromString(@Nullable String value) {
    if (value == null) {
      return null;
    }
    var normalized = value.trim().toUpperCase();
    if (normalized.isEmpty()) {
      return null;
    }
    try {
      return valueOf(normalized);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public static List<Country> parseList(@Nullable String[] values) {
    if (values == null || values.length == 0) {
      return Collections.emptyList();
    }
    return Arrays.stream(values)
        .map(Country::tryFromString)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
  }

  public static List<Country> parseList(@Nullable List<String> values) {
    if (values == null || values.isEmpty()) {
      return Collections.emptyList();
    }
    return values.stream()
        .map(Country::tryFromString)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
  }

  @Nullable
  public static String[] toStringArray(@Nullable List<Country> countries) {
    if (countries == null || countries.isEmpty()) {
      return null;
    }
    return countries.stream().map(Enum::name).toArray(String[]::new);
  }

  /**
   * Returns the ISO 3166-1 alpha-3 code for this country (e.g. {@code BR} -> {@code "BRA"}),
   * derived from the JVM's locale data so no manual mapping table is needed. Used by outbound
   * integrations that expect three-letter country codes.
   */
  public String toAlpha3() {
    return Locale.of("", name()).getISO3Country();
  }
}
