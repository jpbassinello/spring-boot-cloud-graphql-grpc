package br.com.jpbassinello.sbcgg.storage.test;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;

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

  // MinIO's images are published to quay.io, not Docker Hub (`docker pull minio/minio` is
  // denied). Testcontainers' MinIO module still expects the Docker Hub name, so the quay
  // coordinates have to be declared as a compatible substitute or the container refuses to
  // start. Keep this tag in step with infrastructure/docker/compose/shared/docker-compose.yml.
  private static final DockerImageName IMAGE =
      DockerImageName.parse("quay.io/minio/minio:RELEASE.2025-09-07T16-13-09Z")
          .asCompatibleSubstituteFor("minio/minio");

  public static final MinIOContainer INSTANCE =
      new MinIOContainer(IMAGE)
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
