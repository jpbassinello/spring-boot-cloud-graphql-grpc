dependencies {
    api(project(":shared:validation"))
    api("org.springframework.boot:spring-boot-starter-json")
    api("net.devh:grpc-server-spring-boot-starter:${libs.versions.devh.grpc.spring.boot.starter.get()}")
}