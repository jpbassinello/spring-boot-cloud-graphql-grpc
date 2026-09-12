dependencies {
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    // Token-usage counters (llm.tokens) published from OpenAIAdapter.
    implementation("io.micrometer:micrometer-core")
    api("org.apache.commons:commons-text:1.15.0")
    testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
}