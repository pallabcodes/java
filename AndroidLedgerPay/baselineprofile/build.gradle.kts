plugins { id("com.android.test"); id("org.jetbrains.kotlin.android") }
android { namespace = "com.example.ledgerpay.baselineprofile"; compileSdk = 34; defaultConfig { minSdk = 24; targetSdk = 34; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" } targetProjectPath = ":app" }
dependencies { implementation("androidx.benchmark:benchmark-macro-junit4:1.2.4") }
