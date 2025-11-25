package br.com.jpbassinello.sbcgg.mapstruct;

import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class BaseProtobufMapper extends BaseMapper {

  public static ZonedDateTime mapTimestampToZonedDateTime(Timestamp timestamp) {
    var instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    return ZonedDateTime.ofInstant(instant, ZoneOffset.systemDefault());
  }

  public static Timestamp mapZonedDateTimeToTimestamp(ZonedDateTime value) {
    return value == null ? Timestamp.newBuilder().build() : Timestamp.newBuilder()
        .setSeconds(value.toInstant().getEpochSecond())
        .setNanos(value.toInstant().getNano())
        .build();
  }
}
