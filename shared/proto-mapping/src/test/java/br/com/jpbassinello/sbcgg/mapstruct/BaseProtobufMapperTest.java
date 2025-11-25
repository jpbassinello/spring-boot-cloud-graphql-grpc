package br.com.jpbassinello.sbcgg.mapstruct;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BaseProtobufMapperTest {

  @Test
  void mapTimestampToZonedDateTime() {
    assertThat(
        BaseProtobufMapper.mapTimestampToZonedDateTime(
            Timestamp
                .newBuilder()
                .setSeconds(1669874484)
                .setNanos(491202000)
                .build()
        )).isEqualTo(ZonedDateTime.parse("2022-12-01T06:01:24.491202Z[UTC]"));
  }

  @Test
  void mapZonedDateTimeToTimestamp() {
    assertThat(
        BaseProtobufMapper.mapZonedDateTimeToTimestamp(ZonedDateTime.parse("2022-12-01T06:01:24.491202Z[UTC]"))
    ).isEqualTo(
        Timestamp
            .newBuilder()
            .setSeconds(1669874484)
            .setNanos(491202000)
            .build()
    );
  }
}