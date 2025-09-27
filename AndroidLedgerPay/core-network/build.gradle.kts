plugins { id("com.android.library"); id("org.jetbrains.kotlin.android") }
android { namespace = "com.example.ledgerpay.core.network"; compileSdk = 34; defaultConfig { minSdk = 24 } }
dependencies { api("com.squareup.retrofit2:retrofit:2.11.0"); api("com.squareup.okhttp3:okhttp:4.12.0"); api("com.squareup.okhttp3:logging-interceptor:4.12.0"); api("com.squareup.moshi:moshi:1.15.1") }
