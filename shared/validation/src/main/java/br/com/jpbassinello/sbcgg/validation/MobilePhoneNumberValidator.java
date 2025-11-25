package br.com.jpbassinello.sbcgg.validation;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class MobilePhoneNumberValidator implements ConstraintValidator<MobilePhoneNumber, String> {

  @Override
  public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
    if (StringUtils.isBlank(phoneNumber)) {
      return true;
    }
    try {
      var phoneNumberUtil = PhoneNumberUtil.getInstance();
      var parsed = phoneNumberUtil.parse(phoneNumber, Locale.getDefault().toLanguageTag());

      if (!phoneNumberUtil.isValidNumber(parsed)) {
        return false;
      }

      var numberType = phoneNumberUtil.getNumberType(parsed);

      return numberType == PhoneNumberUtil.PhoneNumberType.MOBILE;

    } catch (NumberParseException e) {
      return false;
    }
  }
}
