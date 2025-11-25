plugins {
    alias(libs.plugins.protobuf)
}

subprojects {
    apply(plugin = "com.google.protobuf")

    dependencies {
        api("io.grpc:grpc-protobuf:${rootProject.libs.versions.grpc.get()}")
        api("io.grpc:grpc-stub:${rootProject.libs.versions.grpc.get()}")
        api("com.google.protobuf:protobuf-java:${rootProject.libs.versions.protobuf.get()}")

        // gRPC generated code still uses javax.annotation.Generated
        implementation("javax.annotation:javax.annotation-api:1.3.2")
    }

    protobuf {
        protoc {
            artifact = "com.google.protobuf:protoc:${rootProject.libs.versions.protobuf.get()}"
        }
        plugins {
            create("grpc") {
                artifact = "io.grpc:protoc-gen-grpc-java:${rootProject.libs.versions.grpc.get()}"
            }
        }
        generateProtoTasks {
            all().forEach {
                it.plugins {
                    create("grpc")
                }
            }
        }
    }

    tasks.withType<Checkstyle>().configureEach {
        enabled = false
    }
}