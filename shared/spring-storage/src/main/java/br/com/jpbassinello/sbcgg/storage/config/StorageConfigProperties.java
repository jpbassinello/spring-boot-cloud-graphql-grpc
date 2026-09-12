package br.com.jpbassinello.sbcgg.storage.config;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "storage")
public class StorageConfigProperties {

  @NotBlank
  private String bucket;

  @NotBlank
  private String region;

  @NotBlank
  private String accessKey;

  @NotBlank
  private String secretKey;

  /**
   * S3-compatible endpoint. Leave null to use AWS S3.
   * Examples: <a href="http://localhost:9000">...</a> (MinIO), <a href="https://&lt;account&gt;.r2.cloudflarestorage.com">...</a> (R2).
   */
  @Nullable
  private String endpoint;

  /**
   * Required for MinIO and most non-AWS S3 implementations.
   * AWS S3 supports it but defaults to virtual-host style.
   */
  private boolean pathStyleAccess = true;
}
