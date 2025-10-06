plugins { alias(libs.plugins.android.library); alias(libs.plugins.kotlin.android) }
android { namespace = "com.example.ledgerpay.feature.payments"; compileSdk = 34; defaultConfig { minSdk = 24 } }
dependencies {
    testImplementation(libs.junit4)
    testImplementation(libs.coroutines.test) implementation(project(":core-data")); implementation(libs.compose.material3); implementation(libs.compose.ui) }
