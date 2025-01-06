enum class Environment(
    val gradleName: String,
    val debuggable: Boolean,
    val useProguard: Boolean,
    val remoteAnalytics: Boolean,
    val suffix: String,
    val appNameSuffix: String
) {
    Debug(
        gradleName = "debug",
        suffix = ".dev",
        debuggable = true,
        useProguard = false,
        remoteAnalytics = false,
        appNameSuffix = " Dev"
    ),
    Qa(
        gradleName = "qa",
        suffix = ".dev",
        debuggable = true,
        useProguard = true,
        remoteAnalytics = true,
        appNameSuffix = " Qa"
    ),
    Release(
        gradleName = "release",
        suffix = "",
        appNameSuffix = "",
        remoteAnalytics = true,
        useProguard = true,
        debuggable = false
    );
}
