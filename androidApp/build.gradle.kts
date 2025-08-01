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
}

dependencies {
    implementation(Library.AndroidXCore)
    implementation(Library.AndroidXAppcompat)
    implementation(Library.CoroutinesAndroid)
    implementation(Library.KotlinSerializationJson)

    implementation(Library.SQLDelightAndroid)
    implementation(Library.SQLDelightCoroutines)
    implementation(Library.SQLDelightApapter)

    implementation(Library.KotlinStd)
    implementation(Toolkit.UI)

    implementation(Project.Tools.Coroutines)
    implementation(Library.AccompanistPermission)
    implementation(FirebaseLibrary.FirebaseAnalytics, FirebaseLibrary.FirebaseCrashLytics)
    implementation(Library.Mixpanel)

    implementation(Project.Logger.Core)
    implementation(Project.Logger.Crashlytics)
    implementation(Project.Logger.Logcat)
    implementation(Project.Logger.InternalStore)

    // debugImplementation because LeakCanary should only run in debug builds.
    debugImplementation(Library.Leakcanary)
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
