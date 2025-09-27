
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

subprojects {
    plugins.apply("org.jlleitschuh.gradle.ktlint")
    plugins.apply("io.gitlab.arturbosch.detekt")

    detekt {
        config = files(rootProject.file("detekt.yml"))
        buildUponDefaultConfig = true
        autoCorrect = false
    }

    ktlint {
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
    }
}
