plugins { id("com.android.library"); id("org.jetbrains.kotlin.android"); kotlin("kapt") }
android { namespace = "com.example.ledgerpay.core.data"; compileSdk = 34; defaultConfig { minSdk = 24 } }
dependencies { implementation(project(":core-network")); implementation("androidx.room:room-runtime:2.6.1"); kapt("androidx.room:room-compiler:2.6.1"); implementation("androidx.room:room-ktx:2.6.1") }
