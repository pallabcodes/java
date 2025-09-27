plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.ledgerpay.core.ui"
    compileSdk = 34

    defaultConfig { minSdk = 24 }

    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.13" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3:1.2.1")
}
