pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "AndroidLedgerPay"
include(":app")
include(":core-ui")
include(":core-data")
include(":core-network")
include(":feature-payments")
include(":feature-ledger")
include(":baselineprofile")
