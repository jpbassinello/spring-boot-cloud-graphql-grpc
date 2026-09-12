package br.com.jpbassinello.sbcgg.ai_client.adapter.out;

import br.com.jpbassinello.sbcgg.ai_client.application.port.out.MaxItems;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JsonSchemaGenerator Tests")
class JsonSchemaGeneratorTest {

  @Test
  @DisplayName("should return null for non-record classes")
  void shouldReturnNullForNonRecordClass() {
    assertThat(JsonSchemaGenerator.generate(String.class)).isNull();
    assertThat(JsonSchemaGenerator.generate(Map.class)).isNull();
  }

  @Test
  @DisplayName("should generate schema for simple record with primitives")
  void shouldGenerateSchemaForSimpleRecord() {
    var schema = JsonSchemaGenerator.generate(SimpleRecord.class);

    assertThat(schema).isNotNull();
    assertThat(schema).containsEntry("type", "object");
    assertThat(schema).containsEntry("additionalProperties", false);

    @SuppressWarnings("unchecked")
    var properties = (Map<String, Object>) schema.get("properties");
    assertThat(properties).containsKey("name");
    assertThat(properties).containsKey("score");
    assertThat(properties).containsKey("count");
    assertThat(properties).containsKey("active");

    assertThat(properties.get("name")).isEqualTo(Map.of("type", "string"));
    assertThat(properties.get("score")).isEqualTo(Map.of("type", "number"));
    assertThat(properties.get("count")).isEqualTo(Map.of("type", "integer"));
    assertThat(properties.get("active")).isEqualTo(Map.of("type", "boolean"));

    @SuppressWarnings("unchecked")
    var required = (List<String>) schema.get("required");
    assertThat(required).containsExactly("name", "score", "count", "active");
  }

  @Test
  @DisplayName("should convert camelCase field names to snake_case")
  void shouldConvertCamelCaseToSnakeCase() {
    var schema = JsonSchemaGenerator.generate(CamelCaseRecord.class);

    assertThat(schema).isNotNull();
    @SuppressWarnings("unchecked")
    var properties = (Map<String, Object>) schema.get("properties");
    assertThat(properties).containsKey("work_life_balance");
    assertThat(properties).containsKey("remote_type_metadata");
  }

  @Test
  @DisplayName("should handle nullable fields with anyOf")
  void shouldHandleNullableFields() {
    var schema = JsonSchemaGenerator.generate(NullableRecord.class);

    assertThat(schema).isNotNull();
    @SuppressWarnings("unchecked")
    var properties = (Map<String, Object>) schema.get("properties");

    // Non-nullable field should be direct type
    assertThat(properties.get("name")).isEqualTo(Map.of("type", "string"));

    // Nullable field should use anyOf
    @SuppressWarnings("unchecked")
    var nullableSchema = (Map<String, Object>) properties.get("description");
    assertThat(nullableSchema).containsKey("anyOf");

    @SuppressWarnings("unchecked")
    var anyOf = (List<Map<String, Object>>) nullableSchema.get("anyOf");
    assertThat(anyOf).hasSize(2);
    assertThat(anyOf.get(0)).isEqualTo(Map.of("type", "string"));
    assertThat(anyOf.get(1)).isEqualTo(Map.of("type", "null"));

    // All fields should still be required
    @SuppressWarnings("unchecked")
    var required = (List<String>) schema.get("required");
    assertThat(required).containsExactly("name", "description");
  }

  @Test
  @DisplayName("should handle enum fields")
  void shouldHandleEnumFields() {
    var schema = JsonSchemaGenerator.generate(EnumRecord.class);

    assertThat(schema).isNotNull();
    @SuppressWarnings("unchecked")
    var properties = (Map<String, Object>) schema.get("properties");

    @SuppressWarnings("unchecked")
    var statusSchema = (Map<String, Object>) properties.get("status");
    assertThat(statusSchema).containsEntry("type", "string");
    assertThat(statusSchema).containsEntry("enum", List.of("active", "inactive", "unknown"));
  }

  @Test
  @DisplayName("should handle List fields")
  void shouldHandleListFields() {
    var schema = JsonSchemaGenerator.generate(ListRecord.class);

    assertThat(schema).isNotNull();
    @SuppressWarnings("unchecked")
    var properties = (Map<String, Object>) schema.get("properties");

    @SuppressWarnings("unchecked")
    var tagsSchema = (Map<String, Object>) properties.get("tags");
    assertThat(tagsSchema).containsEntry("type", "array");
    assertThat(tagsSchema).containsEntry("items", Map.of("type", "string"));
  }

  @Test
  @DisplayName("should emit maxItems for @MaxItems-annotated list fields")
  void shouldEmitMaxItemsForAnnotatedList() {
    var schema = JsonSchemaGenerator.generate(MaxItemsRecord.class);

    assertThat(schema).isNotNull();
    @SuppressWarnings("unchecked")
    var properties = (Map<String, Object>) schema.get("properties");

    @SuppressWarnings("unchecked")
    var cappedSchema = (Map<String, Object>) properties.get("evidence");
    assertThat(cappedSchema).containsEntry("type", "array");
    assertThat(cappedSchema).containsEntry("items", Map.of("type", "string"));
    assertThat(cappedSchema).containsEntry("maxItems", 3);

    // Un-annotated list in the same record stays uncapped.
    @SuppressWarnings("unchecked")
    var uncappedSchema = (Map<String, Object>) properties.get("tags");
    assertThat(uncappedSchema).doesNotContainKey("maxItems");
  }

  @Test
  @DisplayName("should handle nested records")
  void shouldHandleNestedRecords() {
    var schema = JsonSchemaGenerator.generate(ParentRecord.class);

    assertThat(schema).isNotNull();
    @SuppressWarnings("unchecked")
    var properties = (Map<String, Object>) schema.get("properties");

    @SuppressWarnings("unchecked")
    var childSchema = (Map<String, Object>) properties.get("child");
    assertThat(childSchema).containsEntry("type", "object");
    assertThat(childSchema).containsEntry("additionalProperties", false);

    @SuppressWarnings("unchecked")
    var childProperties = (Map<String, Object>) childSchema.get("properties");
    assertThat(childProperties).containsKey("value");
    assertThat(childProperties.get("value")).isEqualTo(Map.of("type", "number"));
  }

  @Test
  @DisplayName("should handle list of nested records")
  void shouldHandleListOfRecords() {
    var schema = JsonSchemaGenerator.generate(ListOfRecordsParent.class);

    assertThat(schema).isNotNull();
    @SuppressWarnings("unchecked")
    var properties = (Map<String, Object>) schema.get("properties");

    @SuppressWarnings("unchecked")
    var itemsSchema = (Map<String, Object>) properties.get("items");
    assertThat(itemsSchema).containsEntry("type", "array");

    @SuppressWarnings("unchecked")
    var itemSchema = (Map<String, Object>) itemsSchema.get("items");
    assertThat(itemSchema).containsEntry("type", "object");

    @SuppressWarnings("unchecked")
    var itemProperties = (Map<String, Object>) itemSchema.get("properties");
    assertThat(itemProperties).containsKey("name");
  }

  @Test
  @DisplayName("should return null for records with Map fields")
  void shouldReturnNullForMapFields() {
    assertThat(JsonSchemaGenerator.generate(MapRecord.class)).isNull();
  }

  @Test
  @DisplayName("should return null for records with nested Map fields")
  void shouldReturnNullForNestedMapFields() {
    assertThat(JsonSchemaGenerator.generate(NestedMapRecord.class)).isNull();
  }

  @Test
  @DisplayName("should convert toSnakeCase correctly")
  void shouldConvertToSnakeCase() {
    assertThat(JsonSchemaGenerator.toSnakeCase("camelCase")).isEqualTo("camel_case");
    assertThat(JsonSchemaGenerator.toSnakeCase("workLifeBalance")).isEqualTo("work_life_balance");
    assertThat(JsonSchemaGenerator.toSnakeCase("LlmFullResponse")).isEqualTo("llm_full_response");
    assertThat(JsonSchemaGenerator.toSnakeCase("simple")).isEqualTo("simple");
    assertThat(JsonSchemaGenerator.toSnakeCase("remoteTypeMetadata")).isEqualTo("remote_type_metadata");
    assertThat(JsonSchemaGenerator.toSnakeCase("gpt5Nano")).isEqualTo("gpt5_nano");
  }

  // --- Test record types ---

  record SimpleRecord(String name, double score, int count, boolean active) {}

  record CamelCaseRecord(double workLifeBalance, String remoteTypeMetadata) {}

  record NullableRecord(String name, @Nullable String description) {}

  record EnumRecord(TestStatus status) {}

  enum TestStatus { ACTIVE, INACTIVE, UNKNOWN }

  record ListRecord(List<String> tags) {}

  record MaxItemsRecord(@MaxItems(3) List<String> evidence, List<String> tags) {}

  record ParentRecord(String name, ChildRecord child) {}

  record ChildRecord(double value) {}

  record ListOfRecordsParent(List<ItemRecord> items) {}

  record ItemRecord(String name) {}

  record MapRecord(Map<String, Double> values) {}

  record NestedMapRecord(String name, MapRecord nested) {}
}
