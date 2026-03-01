# gRPC in SBCGG: Spring gRPC, Service Discovery, and Type-Safe Mapping

This is the second post in the SBCGG series. If you haven't read the first one, check it
out: [Introducing SBCGG](1-About-SBCGG.md).

**GitHub**: https://github.com/jpbassinello/spring-boot-cloud-graphql-grpc

---

## Spring gRPC: A Game Changer

For a long time, using gRPC in Spring Boot required third-party libraries or a lot of manual wiring. The Spring team
finally released **Spring gRPC** (currently at version 1.0.2), bringing first-class gRPC support to the Spring
ecosystem. This means gRPC channels, stubs, and servers are now configured the same way we configure everything else in
Spring: through application properties, auto-configuration, and dependency injection.

In SBCGG, the Spring gRPC integration is straightforward. On the **server side**, a gRPC service is just a
Spring-managed bean that extends the generated base class:

```java

@Service
@RequiredArgsConstructor
class UsersGrpcAdapter extends UsersServiceGrpc.UsersServiceImplBase {

  private final LoadUsersUseCase loadAccounts;
  private final ManageUsersUseCase manageAccounts;

  @Override
  @Transactional(readOnly = true)
  public void loadUser(LoadUserRequest request, StreamObserver<LoadUserResponse> responseObserver) {
    var response = LoadUserResponse.newBuilder();

    if (StringUtils.isNotBlank(request.getId())) {
      response.setUser(USER_GRPC_MAPPER.mapToProto(
          loadAccounts.loadUserById(UUID.fromString(request.getId()))
      ));
    }

    responseObserver.onNext(response.build());
    responseObserver.onCompleted();
  }
}
```

On the **client side**, Spring gRPC provides a `GrpcChannelFactory` that creates channels based on named configurations.
The GraphQL gateway uses it to call gRPC services:

```java

@Component
@RequiredArgsConstructor
class UserGrpcAdapter implements LoadUserPort, RegisterUserPort {

  private final GrpcChannelFactory channels;
  private UsersServiceGrpc.UsersServiceBlockingStub usersGrpc;

  private synchronized UsersServiceGrpc.UsersServiceBlockingStub getUsersGrpc() {
    if (usersGrpc == null) {
      usersGrpc = UsersServiceGrpc.newBlockingStub(channels.createChannel("users"));
    }
    return usersGrpc;
  }

  @Override
  public Optional<User> loadUserById(UUID id) {
    return Optional.of(
            getUsersGrpc().loadUser(LoadUserRequest.newBuilder().setId(id.toString()).build())
        ).filter(LoadUserResponse::hasUser)
        .map(LoadUserResponse::getUser)
        .map(USER_GRPC_MAPPER::mapToType);
  }
}
```

The channel name `"users"` maps to a configuration entry in YAML:

```yaml
# Local development (dev profile)
spring:
  grpc:
    client:
      channels:
        users:
          address: "dns:///localhost:8091"
```

This is clean, simple, and feels like Spring. But what happens when services run in a dynamic environment where
addresses aren't static?

## Bridging gRPC with Service Discovery: DiscoveryClientNameResolver

In a containerized or cloud environment, services register themselves with a **service registry** (SBCGG uses Consul)
and discover each other at runtime. Spring Cloud handles this beautifully for HTTP-based communication, but gRPC has its
own name resolution mechanism. The challenge is: **how do we make gRPC channels resolve service names through Consul?**

SBCGG solves this with a custom `DiscoveryClientNameResolver` that plugs Spring Cloud's `DiscoveryClient` into gRPC's
`NameResolver` API.

### How It Works

The solution has three components:

**1. The Resolver** queries Consul for service instances and translates them into gRPC address groups:

```java
class DiscoveryClientNameResolver extends NameResolver {

  private final String serviceName;
  private final DiscoveryClient discoveryClient;

  private void resolve() {
    try {
      var instances = discoveryClient.getInstances(serviceName);
      if (instances.isEmpty()) {
        listener.onError(Status.UNAVAILABLE.withDescription(
            "No instances available for service: " + serviceName));
        return;
      }

      var addressGroups = instances.stream()
          .map(this::toAddressGroup)
          .toList();

      var result = ResolutionResult.newBuilder()
          .setAddressesOrError(StatusOr.fromValue(addressGroups))
          .setAttributes(Attributes.EMPTY)
          .build();
      listener.onResult(result);
    } catch (Exception e) {
      listener.onError(Status.UNAVAILABLE.withDescription(
          "Failed to resolve service: " + serviceName).withCause(e));
    }
  }

  private int getGrpcPort(ServiceInstance instance) {
    var metadata = instance.getMetadata();
    if (metadata != null) {
      var grpcPort = metadata.get("grpc-port");
      if (grpcPort != null && !grpcPort.isEmpty()) {
        return Integer.parseInt(grpcPort);
      }
    }
    return instance.getPort(); // fallback to HTTP port
  }
}
```

A key detail here: services typically run HTTP and gRPC on **different ports**. Consul registers the HTTP port by
default, so the resolver looks for a `grpc-port` key in the service metadata. Each service publishes this metadata in
its Consul registration:

```yaml
spring:
  cloud:
    consul:
      discovery:
        metadata:
          grpc-port: ${spring.grpc.server.port:9090}
```

**2. The Provider** registers a custom URI scheme (`discovery://`) with gRPC's resolver registry:

```java
class DiscoveryClientNameResolverProvider extends NameResolverProvider {

  public static final String SCHEME = "discovery";

  @Override
  public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
    if (!SCHEME.equals(targetUri.getScheme())) {
      return null;
    }
    var serviceName = targetUri.getAuthority();
    return new DiscoveryClientNameResolver(serviceName, discoveryClient);
  }
}
```

**3. The Auto-Configuration** ties it all together, activating only when Consul discovery is enabled:

```java

@Configuration
@ConditionalOnProperty(value = "spring.cloud.consul.discovery.enabled", havingValue = "true")
public class DiscoveryClientGrpcConfig {

  public DiscoveryClientGrpcConfig(DiscoveryClient discoveryClient) {
    var provider = new DiscoveryClientNameResolverProvider(discoveryClient);
    NameResolverRegistry.getDefaultRegistry().register(provider);
  }
}
```

With this in place, the Docker environment simply switches the channel address scheme:

```yaml
# Docker environment (dev-docker profile)
spring:
  grpc:
    client:
      channels:
        users:
          address: "discovery:///users-service"
        messages:
          address: "discovery:///messages-service"
```

The beauty of this approach is that **no client code changes**. The same `GrpcChannelFactory` and blocking stubs work
identically in both local development (`dns:///localhost:8091`) and containerized environments (
`discovery:///users-service`). The resolution strategy is entirely driven by configuration.

## Centralized Error Handling: GrpcServerExceptionAdvice

One thing that was always painful in gRPC was exception handling. Without a centralized mechanism, every RPC method
needed its own try-catch boilerplate, and error responses were inconsistent across services.

Spring gRPC introduces the `GrpcExceptionHandler` interface, which works similarly to Spring MVC's `@ControllerAdvice`.
SBCGG implements it in a shared module so all gRPC services get consistent error handling for free:

```java

@Component
@RequiredArgsConstructor
public class GrpcServerExceptionAdvice implements GrpcExceptionHandler {

  private final ObjectMapper objectMapper;

  @Override
  public StatusException handleException(Throwable ex) {
    return switch (ex) {
      case ConstraintViolationException cve -> handleConstraintViolation(cve);
      case ResourceNotFoundException rnf -> handleNotFound(rnf);
      case BadRequestException bre -> handleBadRequest(bre);
      case TimedOutException toe -> handleTimeout(toe);
      case InternalServerErrorException ise -> handleInternalError(ise);
      case RuntimeException re -> handleUnexpected(re);
      default -> handleUnexpected(ex);
    };
  }
}
```

Each exception type maps to a gRPC status code:

| Exception                      | gRPC Status         | Use Case                  |
|--------------------------------|---------------------|---------------------------|
| `ConstraintViolationException` | `INVALID_ARGUMENT`  | Bean validation failures  |
| `ResourceNotFoundException`    | `NOT_FOUND`         | Entity not found by ID    |
| `BadRequestException`          | `INVALID_ARGUMENT`  | Business rule violations  |
| `TimedOutException`            | `DEADLINE_EXCEEDED` | Timeout on external calls |
| `InternalServerErrorException` | `INTERNAL`          | Known internal failures   |
| Any other exception            | `INTERNAL`          | Unexpected errors         |

The handler also enriches error responses with **gRPC metadata**. For example, a `ResourceNotFoundException` includes
the resource type and ID:

```java
private StatusException handleResourceNotFoundException(ResourceNotFoundException e) {
  var metadata = new Metadata();
  metadata.put(Metadata.Key.of("type", Metadata.ASCII_STRING_MARSHALLER), e.getType());
  metadata.put(Metadata.Key.of("id", Metadata.ASCII_STRING_MARSHALLER), e.getId());
  return Status.NOT_FOUND.withDescription("Resource not found").withCause(e).asException(metadata);
}
```

Validation errors serialize the violation list as JSON in the metadata, so clients can parse and display them:

```java
metadata.put(
    Metadata.Key.of("violations", Metadata.ASCII_STRING_MARSHALLER),
    objectMapper.

writeValueAsString(violations)
);
```

Because this lives in the `shared:grpc-server` module, every gRPC service in the project inherits this behavior by
simply adding the dependency. No per-service configuration needed.

## MapStruct + Protobuf: Type-Safe Mapping Across Boundaries

A microservices architecture with gRPC and GraphQL means dealing with **multiple representations of the same data**. In
SBCGG, a "User" exists as:

1. A **JPA entity** (domain layer, persisted in PostgreSQL)
2. A **Protobuf message** (gRPC transport layer)
3. A **GraphQL type** (API layer, served to clients)

Manually writing conversion code between these types is tedious and error-prone. This is where **MapStruct** shines, and
specifically, the [mapstruct-spi-protobuf](https://github.com/entur/mapstruct-spi-protobuf) library by Entur.

### The Problem with Protobuf and MapStruct

Protobuf-generated Java classes don't follow JavaBean conventions. They use **builders** instead of setters, have
`addAll` methods for repeated fields, and `has` methods for optional fields. Out of the box, MapStruct doesn't know how
to handle these patterns.

The `protobuf-spi-impl` SPI plugin teaches MapStruct how to work with Protobuf types. It's registered as an annotation
processor alongside MapStruct:

```kotlin
// build.gradle.kts
annotationProcessor("org.mapstruct:mapstruct-processor:${rootProject.libs.versions.mapstruct.get()}")
annotationProcessor("no.entur.mapstruct.spi:protobuf-spi-impl:${rootProject.libs.versions.spi.protobuf.mapstruct.get()}")
```

That's it. No runtime dependency, no additional configuration. At compile time, MapStruct now understands how to
generate code that uses Protobuf builders, handles repeated fields with `addAll`, and respects the Protobuf naming
conventions.

### Shared MapStruct Configuration

SBCGG defines a shared configuration for all Protobuf mappers:

```java

@MapperConfig(
    uses = {BaseProtobufMapper.class},
    imports = {BaseProtobufMapper.class},
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ProtobufMapstructConfig {
}
```

Key settings:

- **`ADDER_PREFERRED`**: Uses Protobuf's `addAll` methods for repeated fields instead of trying to call setters on
  immutable lists.
- **`ReportingPolicy.ERROR`**: Fails compilation if any target field is unmapped, catching mapping issues at build time.
- **`NullValueCheckStrategy.ALWAYS`**: Generates null checks, important because Protobuf fields are never null (they
  return defaults).

A `BaseProtobufMapper` provides common conversions, like `Timestamp` to `ZonedDateTime`:

```java
public class BaseProtobufMapper extends BaseMapper {

  public static ZonedDateTime mapTimestampToZonedDateTime(Timestamp timestamp) {
    var instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    return ZonedDateTime.ofInstant(instant, ZoneOffset.systemDefault());
  }

  public static Timestamp mapZonedDateTimeToTimestamp(ZonedDateTime value) {
    return value == null ? Timestamp.newBuilder().build() : Timestamp.newBuilder()
        .setSeconds(value.toInstant().getEpochSecond())
        .setNanos(value.toInstant().getNano())
        .build();
  }
}
```

### Mapping in Action

With this setup, individual mappers are extremely concise. Here's the mapper in the **Users gRPC service** that converts
between JPA entities and Protobuf messages:

```java

@Mapper(config = ProtobufMapstructConfig.class)
interface UserGrpcMapper {

  UserGrpcMapper INSTANCE = Mappers.getMapper(UserGrpcMapper.class);

  br.com.jpbassinello.sbcgg.grpc.interfaces.users.User mapToProto(User user);

  ManageUsersUseCase.RegisterUserInput mapToInput(UserInput userInput);

  UserVerificationCodeType mapToEnum(UserContactMethod userContactMethod);
}
```

And here's the mapper in the **GraphQL gateway** that converts between Protobuf messages and GraphQL types:

```java

@Mapper(config = ProtobufMapstructConfig.class)
interface UserGrpcMapper {

  UserGrpcMapper INSTANCE = Mappers.getMapper(UserGrpcMapper.class);

  @Mapping(target = "messages", ignore = true)
  br.com.jpbassinello.sbcgg.graphql.gateway.domain.types.User mapToType(User user);

  @Mapping(target = "roles", ignore = true)
  UserInput mapToProto(RegisterUserInput userInput);

  UserContactMethod mapToProto(
      br.com.jpbassinello.sbcgg.graphql.gateway.domain.enums.UserContactMethod method);
}
```

The `@Mapping(target = "messages", ignore = true)` annotation tells MapStruct to skip the `messages` field, which is
loaded separately through a GraphQL resolver (a topic for another post).

The entire mapping chain looks like this:

```
GraphQL Input → [MapStruct] → Protobuf Request → [gRPC] → Protobuf Message → [MapStruct] → JPA Entity
     ↑                                                                                          |
     |                                                                                     [Database]
     |                                                                                          |
GraphQL Type ← [MapStruct] ← Protobuf Response ← [gRPC] ← Protobuf Message ← [MapStruct] ← JPA Entity
```

All mapping code is **generated at compile time**. No reflection, no runtime overhead, and if a field is added to the
proto definition but not mapped, the build fails. This is exactly the kind of safety net you want in a microservices
project.

## Putting It All Together

Here's the complete flow when a client queries a user through the GraphQL gateway:

1. **GraphQL controller** receives the query and calls the `LoadUserPort`
2. **UserGrpcAdapter** (gateway) creates a Protobuf request and calls the gRPC stub
3. **Spring gRPC** resolves the channel address (`dns:///` locally, `discovery:///` in Docker)
4. If using `discovery:///`, the **DiscoveryClientNameResolver** queries Consul for the users-service instances
5. **UsersGrpcAdapter** (server) receives the request, calls the use case, and maps the JPA entity to a Protobuf
   response
6. If an error occurs, the **GrpcServerExceptionAdvice** maps it to the appropriate gRPC status code with metadata
7. Back in the gateway, the response is mapped from Protobuf to a GraphQL type using **MapStruct**
8. The GraphQL framework returns the response to the client

Every step in this chain benefits from compile-time type safety, Spring's auto-configuration, and the clean separation
of concerns provided by hexagonal architecture.

## Credits

Special thanks to the **Entur** team for the [mapstruct-spi-protobuf](https://github.com/entur/mapstruct-spi-protobuf)
library. It's one of those small dependencies that makes a huge difference in developer experience when working with
Protobuf and MapStruct together.

---

## What's Next?

In the next posts, I plan to cover:

- **How Spring Boot 4 simplified observability** and why metrics and traces are essential in microservices
- **Hexagonal Architecture in production microservices projects**

---

I'd love to hear your feedback. If you think this could help other developers, please share it.

**GitHub**: https://github.com/jpbassinello/spring-boot-cloud-graphql-grpc

Thank you for reading.