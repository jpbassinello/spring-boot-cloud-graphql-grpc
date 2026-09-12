package br.com.jpbassinello.sbcgg.storage.test;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.MinIOContainer;

/**
 * Singleton MinIO container shared across the JVM. Lifecycle follows the Testcontainers
 * singleton pattern — started once on first reference, torn down by Ryuk at JVM exit.
 *
 * <p>Custom {@code storage.*} properties aren't auto-discovered, so tests bind them via
 * {@link #bindStorageProperties(DynamicPropertyRegistry, String)} from a
 * {@code @DynamicPropertySource} method.
 */
public final class MinioContainer {

  public static final String ACCESS_KEY = "test-access-key";
  public static final String SECRET_KEY = "test-secret-key";

  public static final MinIOContainer INSTANCE =
      new MinIOContainer("minio/minio:RELEASE.2025-09-07T16-13-09Z")
          .withUserName(ACCESS_KEY)
          .withPassword(SECRET_KEY);

  static {
    INSTANCE.start();
  }

  private MinioContainer() {
  }

  public static void bindStorageProperties(DynamicPropertyRegistry registry, String bucket) {
    registry.add("storage.endpoint", INSTANCE::getS3URL);
    registry.add("storage.region", () -> "us-east-1");
    registry.add("storage.access-key", () -> ACCESS_KEY);
    registry.add("storage.secret-key", () -> SECRET_KEY);
    registry.add("storage.bucket", () -> bucket);
    registry.add("storage.path-style-access", () -> true);
  }
}
