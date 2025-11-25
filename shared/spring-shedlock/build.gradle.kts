dependencies {
    api(project(":shared:spring-cache"))
    api("net.javacrumbs.shedlock:shedlock-spring:${libs.versions.shedlock.get()}")
    api("net.javacrumbs.shedlock:shedlock-provider-redis-spring:${libs.versions.shedlock.get()}")
}