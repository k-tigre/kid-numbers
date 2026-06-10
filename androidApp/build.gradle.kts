import com.github.triplet.gradle.androidpublisher.ReleaseStatus
import com.google.firebase.appdistribution.gradle.firebaseAppDistribution

plugins {
    id(Plugin.Id.AndroidApplication.value)
    id(Plugin.Id.KotlinAndroid.value)
    id(Plugin.Id.ComposeCompiler.value) version Plugin.Version.Kotlin.value
    id(Plugin.Id.GoogleServices.value)
    id(Plugin.Id.Crashlytics.value)
    id(Plugin.Id.KotlinParcelize.value)
    id(Plugin.Id.GooglePlayPublisher.value)
    id(Plugin.Id.FirebasePublisher.value)
    id(Plugin.Id.SQLDelight.value)
    id(Plugin.Id.KotlinSerialization.value)
    id("io.github.takahirom.roborazzi") version "1.40.1"
}

android {
    defaultConfig {
        applicationId = Application.id
        namespace = Application.id

        versionName = Application.version.name
        versionCode = Application.version.code
        @Suppress("UnstableApiUsage")
        androidResources.localeFilters.addAll(listOf("en", "ru"))

        buildConfigField("String", "MIXPANEL_TOKEN", "\"${System.getenv("MIXPANEL_TOKEN") ?: ""}\"")
    }

    signingConfigs {
        named(Environment.Debug.gradleName) {
            storeFile = File(rootDir, "/keys/debug.jks")
            storePassword = "debug123"
            keyAlias = "debug"
            keyPassword = "debug123"
        }
        create(Environment.Qa.gradleName) {
            initWith(getAt(Environment.Debug.gradleName))
        }
        val releaseStorePassword = System.getenv("NUMBERS_RELEASE_JKS")
        val releaseKeyPassword = System.getenv("NUMBERS_RELEASE_JKS")

        if (listOf(releaseStorePassword, releaseKeyPassword).any { it.isNullOrBlank() }) {
            System.err.println("Release JKS credentials are not available")
            System.err.println("Release signing config is not available")
        } else {
            create(Environment.Release.gradleName) {
                storeFile = File(rootDir, "/keys/upload.jks")
                storePassword = releaseStorePassword
                keyAlias = "numbers"
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        create(Environment.Qa.gradleName)

        Environment.values().forEach { env ->
            named(env.gradleName) {
                isDebuggable = env.debuggable
                isMinifyEnabled = env.useProguard
                isShrinkResources = env.useProguard

                signingConfig = signingConfigs.findByName(env.gradleName)

                applicationIdSuffix = env.suffix
                manifestPlaceholders["appName"] = "${Application.name}${env.appNameSuffix}"
                buildConfigField("Boolean", "REMOTE_ANALYTICS_ENABLED", env.remoteAnalytics.toString())
                if (env.useProguard) {
                    proguardFiles(
                        "rules.proguard",
                        getDefaultProguardFile(com.android.build.gradle.ProguardFiles.ProguardFile.DONT_OPTIMIZE.fileName)
                    )
                }

                matchingFallbacks.add(Environment.Release.gradleName)

                if (env == Environment.Qa) {
                    firebaseAppDistribution {
                        artifactType = "APK"
                        groups = "test-group"
                        releaseNotes = System.getenv("NOTES")
                    }
                }
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = false
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

roborazzi {
    outputDir.set(rootProject.file("docs/marketing/assets/screenshots"))
}

dependencies {
    implementation(Library.AndroidXCore)
    implementation(Library.AndroidXAppcompat)
    implementation(Library.CoroutinesAndroid)
    implementation(Library.KotlinSerializationJson)
    implementation(Library.AndroidXWork)

    implementation(Library.SQLDelightAndroid)
    implementation(Library.SQLDelightCoroutines)
    implementation(Library.SQLDelightApapter)

    implementation(Library.KotlinStd)
    implementation(Toolkit.UI)

    implementation(Project.Tools.Coroutines)
    implementation(Library.AccompanistPermission)
    implementation(FirebaseLibrary.FirebaseAnalytics, FirebaseLibrary.FirebaseCrashLytics, FirebaseLibrary.FirebaseFirestore, FirebaseLibrary.FirebaseRemoteConfig)
    implementation(Library.CoroutinesPlayServices)
    implementation(Library.Mixpanel)

    implementation(Logger.Core)
    implementation(Logger.Crashlytics)
    implementation(Logger.Logcat)
    implementation(Logger.InternalStore)

    // debugImplementation because LeakCanary should only run in debug builds.
    debugImplementation(Library.Leakcanary)
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.8.3")
    testDebugImplementation("androidx.compose.ui:ui-test-manifest:1.8.3")

    testImplementation(Library.JUnit4)
    testImplementation(Library.AndroidXTestCore)
    testImplementation(Library.Robolectric)
    testImplementation(Library.Roborazzi)
    testImplementation(Library.RoborazziCompose)
    testImplementation(Library.ComposeUiTestJunit4)
    testImplementation(Library.DebugComposeUiToolingPreview)
}

sqldelight {
    databases {
        create("DatabaseNumbers") {
            packageName = "by.tigre.numbers.core.data.storage"
            generateAsync = true
            schemaOutputDirectory.set(file("src/main/sqldelight/databases"))
        }
    }
}

play {
    track.set("alpha")
    userFraction.set(0.5)
    releaseStatus.set(ReleaseStatus.COMPLETED)
}

val syncPlayListingAssetsAction = Action<Task> {
    val outputDir = rootProject.file("docs/marketing/assets/output")
    val playBase = file("src/main/play/listings")
    fun syncScreenshots(sourceDir: File, targetDir: File) {
        targetDir.mkdirs()
        targetDir.listFiles()?.forEach { it.delete() }
        sourceDir.listFiles()
            ?.filter { it.isFile && it.extension.equals("png", ignoreCase = true) }
            ?.sortedBy { it.name }
            ?.forEachIndexed { index, sourceFile ->
                sourceFile.copyTo(targetDir.resolve("${index + 1}.png"), overwrite = true)
            }
    }
    fun syncFeatureGraphic(sourceFile: File, targetFile: File) {
        targetFile.parentFile.mkdirs()
        sourceFile.copyTo(targetFile, overwrite = true)
    }
    syncScreenshots(
        outputDir.resolve("screenshots/ru"),
        playBase.resolve("ru-RU/graphics/phone-screenshots"),
    )
    syncScreenshots(
        outputDir.resolve("screenshots/en"),
        playBase.resolve("en-US/graphics/phone-screenshots"),
    )
    syncFeatureGraphic(
        outputDir.resolve("feature-graphic/feature-graphic-ru.png"),
        playBase.resolve("ru-RU/graphics/feature-graphic/1.png"),
    )
    syncFeatureGraphic(
        outputDir.resolve("feature-graphic/feature-graphic-en.png"),
        playBase.resolve("en-US/graphics/feature-graphic/1.png"),
    )
}

tasks.register("buildMarketingAssets") {
    group = "marketing"
    description = "Render final Google Play PNGs from Roborazzi screenshots"
    dependsOn("recordRoborazziDebug")
    doLast {
        val assetsDir = rootProject.file("docs/marketing/assets")
        val script = assetsDir.resolve("scripts/build_assets.py")
        exec {
            workingDir = assetsDir
            commandLine("python", script.absolutePath)
        }
    }
}

tasks.register("syncPlayListingAssets") {
    group = "marketing"
    description = "Copy generated marketing PNGs into src/main/play/listings for GPP upload"
    dependsOn("buildMarketingAssets")
    doLast(syncPlayListingAssetsAction)
}

tasks.register("buildMarketingScreenshots") {
    group = "marketing"
    description = "Record Roborazzi screenshots and build final Google Play assets"
    dependsOn("syncPlayListingAssets")
}

tasks.register("recordMarketingScreenshots") {
    group = "marketing"
    description = "Re-capture app screenshots with Roborazzi and rebuild marketing assets"
    dependsOn("recordRoborazziDebug", "buildMarketingScreenshots")
}

tasks.register("preparePlayContactMetadata") {
    group = "publishing"
    description = "Write Play contact files from PLAY_CONTACT_EMAIL and PLAY_CONTACT_WEBSITE env vars"
    doLast {
        val playDir = file("src/main/play")
        val email = System.getenv("PLAY_CONTACT_EMAIL")?.trim().orEmpty()
        if (email.isBlank()) {
            throw GradleException("PLAY_CONTACT_EMAIL environment variable is required for Play Store publish")
        }
        playDir.resolve("contact-email.txt").writeText("$email\n")
        val website = System.getenv("PLAY_CONTACT_WEBSITE")?.trim().orEmpty()
        val websiteFile = playDir.resolve("contact-website.txt")
        if (website.isNotBlank()) {
            websiteFile.writeText("$website\n")
        } else if (websiteFile.exists()) {
            websiteFile.delete()
        }
    }
}

tasks.register("publishPlayListing") {
    group = "publishing"
    description = "Upload Play Store listing (texts, screenshots, feature graphic) without a release"
}

tasks.register("publishReleaseApp") {
    group = "publishing"
    description = "Upload release bundle and release notes to Play (no listing update)"
}

afterEvaluate {
    tasks.named("publishReleaseListing").configure {
        dependsOn("preparePlayContactMetadata")
    }
    tasks.named("publishPlayListing").configure {
        dependsOn("publishReleaseListing")
    }
    tasks.named("publishReleaseApp").configure {
        dependsOn("publishReleaseBundle")
    }
}
