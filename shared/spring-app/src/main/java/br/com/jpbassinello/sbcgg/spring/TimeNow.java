package br.com.jpbassinello.sbcgg.spring;

import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class TimeNow {

  public ZonedDateTime get() {
    return ZonedDateTime.now();
  }

}
