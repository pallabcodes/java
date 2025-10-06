plugins { alias(libs.plugins.android.library); alias(libs.plugins.kotlin.android) }
android { namespace = "com.example.ledgerpay.core.network"; compileSdk = 34; defaultConfig { minSdk = 24 } }
dependencies { api(libs.retrofit); api(libs.okhttp); api(libs.okhttp.logging); api(libs.moshi) }
