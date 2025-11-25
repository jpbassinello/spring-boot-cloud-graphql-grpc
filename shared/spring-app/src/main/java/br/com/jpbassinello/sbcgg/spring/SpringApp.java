package br.com.jpbassinello.sbcgg.spring;

import lombok.experimental.UtilityClass;
import org.springframework.boot.SpringApplication;

import java.util.Locale;
import java.util.TimeZone;

@UtilityClass
public final class SpringApp {

  public static void run(Class<?> primarySource, String... args) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale.setDefault(Locale.forLanguageTag("pt-BR"));
    SpringApplication.run(primarySource, args);
  }

}
