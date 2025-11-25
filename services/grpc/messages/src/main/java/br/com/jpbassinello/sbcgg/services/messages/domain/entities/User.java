package br.com.jpbassinello.sbcgg.services.messages.domain.entities;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class User {
  UUID id;
  String email;
  boolean emailVerified;
  String mobilePhoneNumber;
  boolean mobilePhoneNumberVerified;
  String firstName;
  String lastName;
}
