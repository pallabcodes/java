plugins {
    kotlin("jvm") version "1.9.24" apply false
    id("org.springframework.boot") version "3.3.3" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
}

allprojects {
    group = "com.example.kotlinpay"
    version = "0.1.0"
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "17"
        kotlinOptions.freeCompilerArgs += listOf("-Xjsr305=strict")
    }
}

