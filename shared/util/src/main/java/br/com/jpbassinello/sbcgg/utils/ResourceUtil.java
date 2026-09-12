package br.com.jpbassinello.sbcgg.utils;

import java.nio.charset.StandardCharsets;

public final class ResourceUtil {

  public static String readResource(String path) {
    try (var is = ResourceUtil.class.getClassLoader().getResourceAsStream(path)) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException("Failed to read resource: " + path, e);
    }
  }
}
