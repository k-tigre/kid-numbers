@file:Suppress("UnstableApiUsage")

import com.github.triplet.gradle.androidpublisher.ReleaseStatus


plugins {
    id(Plugin.Id.AndroidApplication.value)
    id(Plugin.Id.KotlinAndroid.value)
    id(Plugin.Id.GoogleServices.value)
    id(Plugin.Id.Crashlytics.value)
    id(Plugin.Id.KotlinParcelize.value)
    id(Plugin.Id.GooglePlayPublisher.value)
    id(Plugin.Id.FirebasePublisher.value)
    id(Plugin.Id.SQLDelight.value)
}

android {
    defaultConfig {
        applicationId = Application.id
        namespace = Application.id

        versionName = Application.version.name
        versionCode = Application.version.code
        resourceConfigurations.addAll(listOf("en", "ru"))
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

        val releaseStorePassword = System.getenv("NUMBERS_RELEASE_JKS_STORE_PASSWORD")
        val releaseKeyPassword = System.getenv("NUMBERS_RELEASE_JKS_KEY_PASSWORD")

        if (listOf(releaseStorePassword, releaseKeyPassword).any { it.isNullOrBlank() }) {
            System.err.println("Release JKS credentials are not available")
            System.err.println("Release signing config is not available")
        } else {
            create(Environment.Release.gradleName) {
                storeFile = File(rootDir, "/keys/release.jks")
                storePassword = releaseStorePassword
                keyAlias = "upload_release"
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

    implementation(Library.SQLDelightAndroid)
    implementation(Library.SQLDelightCoroutines)
    implementation(Library.SQLDelightApapter)

    implementation(Library.KotlinStd)
    implementation(Toolkit.UI)

    implementation(Project.Tools.Coroutines)
    implementation(Library.AccompanistPermission)
    implementation(FirebaseLibrary.FirebaseAnalytics, FirebaseLibrary.FirebaseCrashLytics)

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
        }
    }
}

play {
    track.set("internal")
    userFraction.set(1.0)
    releaseStatus.set(ReleaseStatus.COMPLETED)
}
