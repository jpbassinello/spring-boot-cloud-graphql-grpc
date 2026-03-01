# gRPC Interfaces Guide

## Structure

Proto files are defined in `grpc-interfaces/<service>/src/main/proto/`.

Each service has its own submodule with Protocol Buffer definitions.

## Updating gRPC Definitions

```bash
# 1. Modify .proto files in grpc-interfaces/<service>/src/main/proto/
# 2. Rebuild the interface module
./gradlew :grpc-interfaces:<service>:build

# 3. Rebuild services that depend on it
./gradlew :services:grpc:<service>:build
```

## Adding a New gRPC Interface

1. Create the proto directory: `grpc-interfaces/<service>/src/main/proto/`

2. Define the schema:

```protobuf
syntax = "proto3";
package br.com.jpbassinello.sbcgg.<service>;

service <Service>Service {
  rpc Create<Entity>(Create<Entity>Request) returns (Create<Entity>Response);
}

message Create<Entity>Request {
  string field = 1;
}

message Create<Entity>Response {
  string id = 1;
}
```

3. Add to `settings.gradle.kts`: `include(":grpc-interfaces:<service>")`

4. Build: `./gradlew :grpc-interfaces:<service>:build`

## Troubleshooting

**Proto compilation fails:**
```bash
./gradlew :grpc-interfaces:clean :grpc-interfaces:build
```
