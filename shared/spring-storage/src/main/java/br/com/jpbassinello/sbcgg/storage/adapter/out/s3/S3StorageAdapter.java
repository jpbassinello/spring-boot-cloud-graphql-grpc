package br.com.jpbassinello.sbcgg.storage.adapter.out.s3;

import br.com.jpbassinello.sbcgg.storage.application.port.out.ObjectStoragePort;
import br.com.jpbassinello.sbcgg.storage.config.StorageConfigProperties;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
class S3StorageAdapter implements ObjectStoragePort {

  private final S3Client s3Client;
  private final S3Presigner presigner;
  private final String bucket;

  S3StorageAdapter(S3Client s3Client, S3Presigner presigner, StorageConfigProperties properties) {
    this.s3Client = s3Client;
    this.presigner = presigner;
    this.bucket = properties.getBucket();
  }

  @Override
  public StoredObject put(String key, InputStream content, long size, String contentType) {
    var request = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType(contentType)
        .contentLength(size)
        .build();

    var response = s3Client.putObject(request, RequestBody.fromInputStream(content, size));
    log.debug("Stored object bucket={} key={} size={} etag={}", bucket, key, size, response.eTag());
    return new StoredObject(key, response.eTag(), size);
  }

  @Override
  public Optional<RetrievedObject> get(String key) {
    var request = GetObjectRequest.builder().bucket(bucket).key(key).build();
    try {
      var response = s3Client.getObject(request, ResponseTransformer.toBytes());
      return Optional.of(new RetrievedObject(
          new ByteArrayInputStream(response.asByteArray()),
          response.response().contentType(),
          response.response().contentLength()
      ));
    } catch (NoSuchKeyException e) {
      return Optional.empty();
    }
  }

  @Override
  public boolean delete(String key) {
    if (!exists(key)) {
      return false;
    }
    s3Client.deleteObject(b -> b.bucket(bucket).key(key));
    return true;
  }

  @Override
  public boolean exists(String key) {
    try {
      s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
      return true;
    } catch (NoSuchKeyException e) {
      return false;
    }
  }

  @Override
  public URI presignedPutUrl(String key, @Nullable String contentType, Duration expiry) {
    // Intentionally not signing Content-Type: browser-reported MIME for .docx is
    // unstable across browsers, and a mismatch with the signed header yields 403
    // SignatureDoesNotMatch — silently breaking uploads.
    var presignRequest = PutObjectPresignRequest.builder()
        .signatureDuration(expiry)
        .putObjectRequest(PutObjectRequest.builder().bucket(bucket).key(key).build())
        .build();
    return toUri(presigner.presignPutObject(presignRequest).url());
  }

  @Override
  public URI presignedGetUrl(String key, Duration expiry) {
    var getRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();
    var presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(expiry)
        .getObjectRequest(getRequest)
        .build();
    return toUri(presigner.presignGetObject(presignRequest).url());
  }

  private static URI toUri(java.net.URL url) {
    try {
      return url.toURI();
    } catch (URISyntaxException e) {
      throw new IllegalStateException("Invalid presigned URL: " + url, e);
    }
  }
}
