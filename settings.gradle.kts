@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Numbers"
include(":androidApp")


include(":tools:presentation:compose")
include(":tools:presentation:decompose")
include(":tools:coroutines")

include(":logger:core")
include(":logger:logcat")
include(":logger:crashlytics")
include(":logger:internal-store")
