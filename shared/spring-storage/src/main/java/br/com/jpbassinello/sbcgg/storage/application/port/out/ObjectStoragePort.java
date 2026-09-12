package br.com.jpbassinello.sbcgg.storage.application.port.out;

import jakarta.annotation.Nullable;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;

public interface ObjectStoragePort {

  StoredObject put(String key, InputStream content, long size, String contentType);

  Optional<RetrievedObject> get(String key);

  boolean delete(String key);

  boolean exists(String key);

  URI presignedPutUrl(String key, @Nullable String contentType, Duration expiry);

  URI presignedGetUrl(String key, Duration expiry);

  record StoredObject(String key, String etag, long size) {}

  record RetrievedObject(InputStream content, String contentType, long size) {}
}
