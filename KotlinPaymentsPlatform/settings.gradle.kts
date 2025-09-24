rootProject.name = "kotlin-payments-platform"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "1.9.24"
        id("org.springframework.boot") version "3.3.3"
        id("io.spring.dependency-management") version "1.1.6"
        id("org.jetbrains.kotlin.plugin.spring") version "1.9.24"
    }
}

include(
    "shared",
    "ledger-service",
    "payments-service",
    "risk-service",
    "notification-service",
    "api-gateway"
)

