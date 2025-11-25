dependencies {
    api(project(":shared:util"))
    api(project(":shared:mapping"))

    api("com.google.protobuf:protobuf-java:${libs.versions.protobuf.get()}")
    api("com.google.protobuf:protobuf-java-util:${libs.versions.protobuf.get()}")
}