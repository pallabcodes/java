plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // JWT and Security Dependencies
    api("io.jsonwebtoken:jjwt-api:0.11.5")
    api("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Spring Security
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("jakarta.validation:jakarta.validation-api:3.0.2")

    // PCI DSS Compliance - Encryption and Security
    api("org.bouncycastle:bcprov-jdk18on:1.78.1")
    api("org.bouncycastle:bcpkix-jdk18on:1.78.1")

    // Logging and Audit
    api("org.slf4j:slf4j-api:2.0.16")
    api("ch.qos.logback:logback-classic:1.4.14")

    // Spring Boot Starter for easier dependency management
    api("org.springframework.boot:spring-boot-starter")
}

