package br.com.jpbassinello.sbcgg.storage.adapter.out.s3;

import br.com.jpbassinello.sbcgg.storage.application.port.out.ObjectStoragePort;
import br.com.jpbassinello.sbcgg.storage.config.StorageConfig;
import br.com.jpbassinello.sbcgg.storage.test.MinioContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = StorageConfig.class)
class S3StorageAdapterIT {

  private static final String BUCKET = "test-bucket";

  @Autowired
  private ObjectStoragePort storage;

  @Autowired
  private S3Client s3Client;

  @DynamicPropertySource
  static void storageProps(DynamicPropertyRegistry registry) {
    MinioContainer.bindStorageProperties(registry, BUCKET);
  }

  @BeforeEach
  void createBucket() {
    var exists = s3Client.listBuckets().buckets().stream()
        .anyMatch(b -> BUCKET.equals(b.name()));
    if (!exists) {
      s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());
    }
  }

  @Test
  void shouldRoundTripObject() throws Exception {
    // Arrange
    var key = "cv/round-trip.txt";
    var payload = "hello-cv".getBytes(StandardCharsets.UTF_8);

    // Act
    var stored = storage.put(key, new ByteArrayInputStream(payload), payload.length, "text/plain");

    // Assert
    assertThat(stored.key()).isEqualTo(key);
    assertThat(stored.size()).isEqualTo(payload.length);
    assertThat(stored.etag()).isNotBlank();

    assertThat(storage.exists(key)).isTrue();

    var fetched = storage.get(key).orElseThrow();
    assertThat(fetched.contentType()).isEqualTo("text/plain");
    assertThat(new String(fetched.content().readAllBytes(), StandardCharsets.UTF_8))
        .isEqualTo("hello-cv");
  }

  @Test
  void shouldDeleteObject() {
    // Arrange
    var key = "cv/to-delete.txt";
    storage.put(key, new ByteArrayInputStream("x".getBytes(StandardCharsets.UTF_8)), 1, "text/plain");

    // Act
    var deleted = storage.delete(key);

    // Assert
    assertThat(deleted).isTrue();
    assertThat(storage.exists(key)).isFalse();
    assertThat(storage.delete(key)).isFalse();
  }

  @Test
  void shouldUploadViaPresignedPutUrl() throws Exception {
    // Arrange
    var key = "cv/presigned-put.txt";
    var url = storage.presignedPutUrl(key, "text/plain", Duration.ofMinutes(5)).toURL();

    // Act
    var connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("PUT");
    connection.setRequestProperty("Content-Type", "text/plain");
    connection.setDoOutput(true);
    try (var out = connection.getOutputStream()) {
      out.write("via-presigned".getBytes(StandardCharsets.UTF_8));
    }

    // Assert
    assertThat(connection.getResponseCode()).isEqualTo(200);
    assertThat(storage.exists(key)).isTrue();
  }

  @Test
  void shouldDownloadViaPresignedGetUrl() throws Exception {
    // Arrange
    var key = "cv/presigned-get.txt";
    var payload = "downloaded-bytes".getBytes(StandardCharsets.UTF_8);
    storage.put(key, new ByteArrayInputStream(payload), payload.length, "text/plain");

    // Act
    var url = storage.presignedGetUrl(key, Duration.ofMinutes(5)).toURL();
    var connection = (HttpURLConnection) url.openConnection();

    // Assert
    assertThat(connection.getResponseCode()).isEqualTo(200);
    try (var in = connection.getInputStream()) {
      assertThat(new String(in.readAllBytes(), StandardCharsets.UTF_8))
          .isEqualTo("downloaded-bytes");
    }
  }

  @Test
  void shouldReturnEmptyForMissingKey() {
    assertThat(storage.get("does/not/exist")).isEmpty();
    assertThat(storage.exists("does/not/exist")).isFalse();
  }
}
