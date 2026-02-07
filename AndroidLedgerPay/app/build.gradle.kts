plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    id("jacoco")
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    kotlin("kapt")
}

fun toBuildConfigString(value: String): String {
    val escaped = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
    return "\"$escaped\""
}

val releasePrimaryPinProvider = providers.gradleProperty("LEDGERPAY_PIN_PRIMARY")
    .orElse(providers.environmentVariable("LEDGERPAY_PIN_PRIMARY"))
    .map { it.trim() }
    .orElse("")

val releaseBackupPinProvider = providers.gradleProperty("LEDGERPAY_PIN_BACKUP")
    .orElse(providers.environmentVariable("LEDGERPAY_PIN_BACKUP"))
    .map { it.trim() }
    .orElse("")

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-Xjvm-default=all")
    }
    namespace = "com.example.ledgerpay"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.ledgerpay"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        // Performance: Increase heap size for builds
        javaCompileOptions.annotationProcessorOptions.arguments += mapOf(
            "room.incremental" to "true"
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            buildConfigField("String", "API_BASE_URL", "\"https://api.ledgerpay.com/\"")
            buildConfigField("String", "LEDGERPAY_PIN_PRIMARY", toBuildConfigString(releasePrimaryPinProvider.get()))
            buildConfigField("String", "LEDGERPAY_PIN_BACKUP", toBuildConfigString(releaseBackupPinProvider.get()))
            buildConfigField("boolean", "PERFORMANCE_MONITORING_ENABLED", "false")
            buildConfigField("boolean", "DEBUG_NETWORK_ENABLED", "false")
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false

            buildConfigField("String", "API_BASE_URL", "\"https://api-dev.ledgerpay.com/\"")
            buildConfigField("String", "LEDGERPAY_PIN_PRIMARY", "\"\"")
            buildConfigField("String", "LEDGERPAY_PIN_BACKUP", "\"\"")
            buildConfigField("boolean", "PERFORMANCE_MONITORING_ENABLED", "true")
            buildConfigField("boolean", "DEBUG_NETWORK_ENABLED", "true")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
        // Performance: Exclude unnecessary resources
        resources.excludes += listOf(
            "META-INF/DEPENDENCIES",
            "META-INF/LICENSE",
            "META-INF/LICENSE.txt",
            "META-INF/license.txt",
            "META-INF/NOTICE",
            "META-INF/NOTICE.txt",
            "META-INF/notice.txt",
            "META-INF/ASL2.0",
            "META-INF/*.kotlin_module"
        )
    }
}

val releaseTasksRequested = gradle.startParameter.taskNames.any { task ->
    task.contains("release", ignoreCase = true)
}
if (releaseTasksRequested) {
    check(releasePrimaryPinProvider.get().isNotBlank()) {
        "Missing LEDGERPAY_PIN_PRIMARY. Set via -PLEDGERPAY_PIN_PRIMARY or env var."
    }
    check(releaseBackupPinProvider.get().isNotBlank()) {
        "Missing LEDGERPAY_PIN_BACKUP. Set via -PLEDGERPAY_PIN_BACKUP or env var."
    }
}

dependencies {
    implementation(libs.android.material)
    implementation(platform(libs.compose.bom))
    androidTestImplementation(platform(libs.compose.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.nav.compose)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
    implementation(libs.hilt.nav.compose)
    implementation(libs.timber)
    implementation(libs.room.runtime)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Production Grade: Observability & Performance
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    debugImplementation(libs.leakcanary.android)

    implementation(project(":core-ui"))
    implementation(project(":core-data"))
    implementation(project(":core-network"))
    implementation(project(":feature-payments"))
    implementation(project(":feature-ledger"))

    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kapt { correctErrorTypes = true }

// JaCoCo test coverage
android {
    buildTypes {
        getByName("debug") {
            enableUnitTestCoverage = true
        }
    }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R\$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/di/**"
    )

    val debugTree = fileTree("${project.layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(project.layout.buildDirectory.get()) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
    })
}
