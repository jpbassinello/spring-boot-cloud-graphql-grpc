subprojects {
    group = "br.com.jpbassinello.sbcgg.tests"

    apply(plugin = "io.freefair.lombok")

    dependencies {
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }
}