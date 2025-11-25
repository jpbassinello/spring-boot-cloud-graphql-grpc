subprojects {
    group = "br.com.jpbassinello.sbcgg.shared"

    apply(plugin = "io.freefair.lombok")

    dependencies {
        compileOnly("jakarta.annotation:jakarta.annotation-api")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }
}