plugins { alias(libs.plugins.android.library); alias(libs.plugins.kotlin.android); kotlin("kapt") }
android { namespace = "com.example.ledgerpay.core.data"; compileSdk = 34; defaultConfig { minSdk = 24 } }
dependencies {
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("org.robolectric:robolectric:4.11.1")
    implementation(libs.datastore.preferences) implementation(project(":core-network")); implementation(libs.room.runtime); kapt(libs.room.compiler); implementation(libs.room.ktx) }
