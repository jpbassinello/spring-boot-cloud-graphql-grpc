import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

subprojects {
    group = "br.com.jpbassinello.sbcgg.services"

    apply(plugin = "io.freefair.lombok")

    dependencies {
        api(project(":shared:spring-app"))
        api("org.springframework.boot:spring-boot-devtools")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }

    // Apply Spring Boot plugin to leaf subprojects (actual applications)
    subprojects {
        // Only apply to projects that don't have their own subprojects (leaf nodes)
        if (subprojects.isEmpty()) {
            apply(plugin = "org.springframework.boot")

            tasks.named<BootBuildImage>("bootBuildImage") {
                buildpacks.set(
                    listOf(
                        "urn:cnb:builder:paketo-buildpacks/java",
                        "docker.io/paketobuildpacks/health-checker:latest"
                    )
                )
                imageName.set("sbcgg/${project.name}:${project.version}")
                environment.set(
                    mapOf(
                        "BP_JVM_VERSION" to "25",
                        "BP_HEALTH_CHECKER_ENABLED" to "true"
                    )
                )
                tags.set(
                    listOf(
                        "sbcgg/${project.name}:latest",
                        "sbcgg/${project.name}:${project.version}"
                    )
                )
            }
        }
    }
}