plugins { id("com.android.library"); id("org.jetbrains.kotlin.android") }
android { namespace = "com.example.ledgerpay.feature.payments"; compileSdk = 34; defaultConfig { minSdk = 24 } }
dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1") implementation(project(":core-data")); implementation("androidx.compose.material3:material3:1.2.1"); implementation("androidx.compose.ui:ui") }
