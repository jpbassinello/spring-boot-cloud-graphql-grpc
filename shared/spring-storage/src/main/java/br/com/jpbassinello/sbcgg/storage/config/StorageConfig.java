package br.com.jpbassinello.sbcgg.storage.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(StorageConfigProperties.class)
@ComponentScan(basePackages = "br.com.jpbassinello.sbcgg.storage.adapter.out")
public class StorageConfig {

  @Bean(destroyMethod = "close")
  public S3Client s3Client(StorageConfigProperties properties) {
    var builder = S3Client.builder()
        .region(Region.of(properties.getRegion()))
        .credentialsProvider(credentials(properties))
        .serviceConfiguration(S3Configuration.builder()
            .pathStyleAccessEnabled(properties.isPathStyleAccess())
            .build());

    if (properties.getEndpoint() != null) {
      builder.endpointOverride(URI.create(properties.getEndpoint()));
    }

    return builder.build();
  }

  @Bean(destroyMethod = "close")
  public S3Presigner s3Presigner(StorageConfigProperties properties) {
    var builder = S3Presigner.builder()
        .region(Region.of(properties.getRegion()))
        .credentialsProvider(credentials(properties))
        .serviceConfiguration(S3Configuration.builder()
            .pathStyleAccessEnabled(properties.isPathStyleAccess())
            .build());

    if (properties.getEndpoint() != null) {
      builder.endpointOverride(URI.create(properties.getEndpoint()));
    }

    return builder.build();
  }

  private static StaticCredentialsProvider credentials(StorageConfigProperties properties) {
    return StaticCredentialsProvider.create(
        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
    );
  }
}
