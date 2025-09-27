plugins { id("com.android.library"); id("org.jetbrains.kotlin.android") }
android { namespace = "com.example.ledgerpay.feature.ledger"; compileSdk = 34; defaultConfig { minSdk = 24 } }
dependencies { implementation(project(":core-data")); implementation("androidx.compose.material3:material3:1.2.1"); implementation("androidx.compose.ui:ui") }
