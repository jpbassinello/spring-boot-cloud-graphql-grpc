package br.com.jpbassinello.sbcgg.validation;

import jakarta.validation.Validation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class MobilePhoneNumberValidatorTest {

  @ParameterizedTest
  @CsvSource({
      "+5519991038010",
      "+5521991038010",
  })
  void validNumbers(String number) {
    try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
      var validator = validatorFactory.getValidator();

      var validNumber = new Phone(number);
      var violations = validator.validate(validNumber);
      assertThat(violations).isEmpty();
    }
  }

  @ParameterizedTest
  @CsvSource({
      "+552191038010", // all mobile numbers in Brazil have 9 digits now
      "+551931038010", // fixed line number
      "5521991038010", // no country code
  })
  void invalidNumbers(String number) {
    try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
      var validator = validatorFactory.getValidator();

      var validNumber = new Phone(number);
      var violations = validator.validate(validNumber);
      assertThat(violations).hasSize(1);
    }
  }

  record Phone(
      @MobilePhoneNumber
      String number
  ) {}

}
