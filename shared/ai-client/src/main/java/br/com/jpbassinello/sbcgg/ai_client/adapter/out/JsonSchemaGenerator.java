package br.com.jpbassinello.sbcgg.ai_client.adapter.out;

import br.com.jpbassinello.sbcgg.ai_client.application.port.out.MaxItems;
import jakarta.annotation.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates JSON Schema definitions compatible with OpenAI Structured Outputs.
 *
 * <p>Strict-mode constraints enforced by this generator:
 * <ul>
 *   <li>Every object has {@code additionalProperties: false}</li>
 *   <li>All properties are listed in {@code required}</li>
 *   <li>Nullable fields use {@code anyOf: [{type}, {type: "null"}]}</li>
 * </ul>
 *
 * <p>Returns {@code null} for types that cannot be represented in strict mode
 * (e.g. classes with {@link Map} fields).
 */
final class JsonSchemaGenerator {

  private JsonSchemaGenerator() {
  }

  /**
   * Generates an OpenAI-compatible JSON Schema for the given record class.
   *
   * @return the schema as a nested Map, or {@code null} if the class
   *     cannot be represented in strict mode
   */
  @Nullable
  static Map<String, Object> generate(Class<?> clazz) {
    if (!clazz.isRecord()) {
      return null;
    }
    return buildObjectSchema(clazz);
  }

  @Nullable
  private static Map<String, Object> buildObjectSchema(Class<?> recordClass) {
    var components = recordClass.getRecordComponents();
    var properties = new LinkedHashMap<String, Object>();
    var required = new ArrayList<String>();

    for (var component : components) {
      var name = toSnakeCase(component.getName());
      var typeSchema = resolveType(component.getType(), component.getGenericType(), component);
      if (typeSchema == null) {
        return null;
      }

      if (isNullable(component)) {
        properties.put(name, Map.of(
            "anyOf", List.of(typeSchema, Map.of("type", "null"))
        ));
      } else {
        properties.put(name, typeSchema);
      }
      required.add(name);
    }

    var schema = new LinkedHashMap<String, Object>();
    schema.put("type", "object");
    schema.put("properties", properties);
    schema.put("required", required);
    schema.put("additionalProperties", false);
    return schema;
  }

  @Nullable
  private static Map<String, Object> resolveType(
      Class<?> type, java.lang.reflect.Type genericType, @Nullable RecordComponent component) {
    if (type == String.class) {
      return Map.of("type", "string");
    }
    if (type == double.class || type == Double.class
        || type == float.class || type == Float.class) {
      return Map.of("type", "number");
    }
    if (type == int.class || type == Integer.class
        || type == long.class || type == Long.class) {
      return Map.of("type", "integer");
    }
    if (type == boolean.class || type == Boolean.class) {
      return Map.of("type", "boolean");
    }

    if (type.isEnum()) {
      var values = Arrays.stream(type.getEnumConstants())
          .map(e -> ((Enum<?>) e).name().toLowerCase())
          .toList();
      return Map.of("type", "string", "enum", values);
    }

    if (List.class.isAssignableFrom(type)
        && genericType instanceof ParameterizedType pt) {
      var arg = pt.getActualTypeArguments()[0];
      if (arg instanceof Class<?> itemClass) {
        var itemSchema = resolveType(itemClass, itemClass, null);
        if (itemSchema == null) {
          return null;
        }
        var arraySchema = new LinkedHashMap<String, Object>();
        arraySchema.put("type", "array");
        arraySchema.put("items", itemSchema);
        if (component != null) {
          var maxItems = component.getAnnotation(MaxItems.class);
          if (maxItems != null) {
            arraySchema.put("maxItems", maxItems.value());
          }
        }
        return arraySchema;
      }
      return null;
    }

    if (Map.class.isAssignableFrom(type)) {
      return null;
    }

    if (type.isRecord()) {
      return buildObjectSchema(type);
    }

    return null;
  }

  private static boolean isNullable(RecordComponent component) {
    try {
      var field = component.getDeclaringRecord()
          .getDeclaredField(component.getName());
      return field.isAnnotationPresent(Nullable.class);
    } catch (NoSuchFieldException e) {
      return false;
    }
  }

  static String toSnakeCase(String camelCase) {
    return camelCase.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
  }
}