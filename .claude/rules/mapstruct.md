# MapStruct Usage (gRPC / Protobuf)

All proto ↔ domain mapping goes through MapStruct. The **entur protobuf SPI** is
registered as a global annotation processor, so mappers should be *thin*: declare
abstract methods and let the generator do the work. Hand-written `switch`
statements, `@EnumMapping`/`@ValueMapping` prefix logic, and manual `has*()`
presence checks in controllers are almost always redundant — and drift out of sync
when the proto changes.

## The build wiring (why this works)

- `build.gradle.kts` registers two processors for every module:
  - `org.mapstruct:mapstruct-processor`
  - `no.entur.mapstruct.spi:protobuf-spi-impl` (version in `settings.gradle.kts`,
    catalog key `spi-protobuf-mapstruct`)
- Proto mappers use `@Mapper(config = ProtobufMapstructConfig.class)`
  (`shared/proto-mapping`), which sets `unmappedTargetPolicy = ERROR`,
  `collectionMappingStrategy = ADDER_PREFERRED`, `nullValueCheckStrategy = ALWAYS`,
  and `uses = BaseProtobufMapper` (Timestamp ↔ ZonedDateTime helpers).
- Non-proto mappers use `DefaultMapstructConfig` (`shared/mapping`).

## What the entur SPI handles automatically — DO NOT hand-write

1. **Enum prefix stripping (enum ↔ enum).** Proto `MESSAGE_STATUS_SENT` maps to
   domain `SENT` and back. Just declare the method:
   ```java
   MessageStatus fromProto(br.com...messages.MessageStatus status);   // proto -> domain
   br.com...messages.MessageStatus toProto(MessageStatus status);     // domain -> proto
   ```
   No `@EnumMapping(nameTransformationStrategy = STRIP_PREFIX...)` needed.

2. **`*_UNSPECIFIED` and `UNRECOGNIZED` → `null`.** The SPI maps the proto zero
   value and `UNRECOGNIZED` to `null` on the proto→domain side, and `null` →
   the `*_UNSPECIFIED` zero value on the domain→proto side. No
   `@ValueMapping(source = "..._UNSPECIFIED", target = MappingConstants.NULL)`.

3. **`optional` scalar & message-field presence.** For a proto `optional string
   name`, the generated code emits `if (request.hasName()) target.name(...)`, so an
   absent field becomes `null`. **Do not** replicate this with `request.hasX()`
   ternaries in `@Mapping(expression = ...)` or with manual builder code in the
   controller — declare the field and let MapStruct generate the presence check.

4. **Repeated fields & `map<>` fields** map to `List`/`Map` via the standard
   collection strategy.

## What still needs manual handling

- **Empty-list → `null` business rule.** Proto `repeated` never returns `null`
  (always an empty list). If the domain needs `null` to mean "not provided"
  (e.g. a partial update request), keep a small `default emptyToNull(List<String>)`
  helper and reference it from `@Mapping(expression = ...)`. This is business
  logic, not something the SPI infers.

- **Proto enum → `String` (or `String` → enum).** The SPI's prefix stripping only
  applies to **enum ↔ enum**. Mapping a proto enum to a plain `String` uses
  `Enum.name()`, which keeps the prefix (`"MESSAGE_STATUS_SENT"`). If a domain
  field is a `String` and you want the stripped value, you still need an explicit
  `default` method with a `switch`.

- **Domain enum → proto with a fail-fast guard.** When you want an unmapped future
  domain value to throw rather than silently become the zero value, keep
  `@ValueMapping(source = ANY_REMAINING, target = MappingConstants.THROW_EXCEPTION)`
  (see `gateway/.../adapter/out/user/UserGrpcMapper`).

- **Enum → `String[]` array on an entity** (e.g. a `List<Country>` column) —
  the entity exposes typed getters/setters (`List<Country>`) backed by a
  `String[]`; MapStruct maps `List<Country>` ↔ `List<String>` element-wise via
  `Enum.name()` automatically, so no custom `mapCountriesToProto` is needed.

- **Anything genuinely custom** (computed flags, cross-field logic): use a
  `default` method or `@Mapping(expression = "java(...)")`.

## Checklist when adding / reviewing a proto mapper

- [ ] Prefer a bare abstract method over a `default` switch for enum ↔ enum.
- [ ] No `@EnumMapping`/`@ValueMapping` just to strip a prefix or null out
      `*_UNSPECIFIED`/`UNRECOGNIZED` — the SPI already does it.
- [ ] No `request.hasX()` ternaries for `optional`/message fields — declare the
      target field and let the generator emit the presence check.
- [ ] No manual proto→domain conversion in gRPC controllers; call the mapper.
- [ ] Keep `emptyToNull` only where empty-list-means-null is a real domain rule.
- [ ] Verify by building the module and reading the generated `*MapperImpl.java`
      under `build/generated/sources/annotationProcessor/...` — `unmappedTargetPolicy
      = ERROR` will fail the build if a field is silently dropped.
