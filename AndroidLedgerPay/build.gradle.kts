
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.jacoco) apply false
}

subprojects {
    plugins.apply("org.jlleitschuh.gradle.ktlint")
    plugins.apply("io.gitlab.arturbosch.detekt")

    extensions.configure(io.gitlab.arturbosch.detekt.extensions.DetektExtension::class) {
        config = files(rootProject.file("detekt.yml"))
        buildUponDefaultConfig = true
        autoCorrect = false
    }

    extensions.configure(org.jlleitschuh.gradle.ktlint.KtlintExtension::class) {
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
    }
}
