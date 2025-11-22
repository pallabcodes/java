plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    namespace = "com.example.ledgerpay.feature.payments"
    compileSdk = 34
    defaultConfig { minSdk = 24 }
}

dependencies {
    implementation(project(":core-data"))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.hilt.nav.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    testImplementation(libs.junit4)
    testImplementation(libs.coroutines.test)
    testImplementation("io.mockk:mockk:1.13.10")
}
