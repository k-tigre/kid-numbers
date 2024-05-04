@file:Suppress("SpellCheckingInspection")

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.kotlin.dsl.project

enum class Library(group: String, artifact: String, version: Version) {
    AndroidXAppcompat("androidx.appcompat", "appcompat", Version.AndroidXAppcompat),
    AndroidXCore("androidx.core", "core-ktx", Version.AndroidXCore),
    AndoirdXAnnotation("androidx.annotation", "annotation", Version.AndroidXAnnotation),
    AndroidXSplash("androidx.core", "core-splashscreen", Version.AndroidXSplash),

    KotlinStd("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", Version.Kotlin),

    CoroutinesCore("org.jetbrains.kotlinx", "kotlinx-coroutines-core", Version.Coroutines),
    CoroutinesAndroid("org.jetbrains.kotlinx", "kotlinx-coroutines-android", Version.Coroutines),

    SQLDelightAndroid("app.cash.sqldelight", "android-driver", Version.SQLDelight),
    SQLDelightCoroutines("app.cash.sqldelight", "coroutines-extensions", Version.SQLDelight),
    SQLDelightApapter("app.cash.sqldelight", "primitive-adapters", Version.SQLDelight),

    Leakcanary("com.squareup.leakcanary", "leakcanary-android", Version.Leakcanary),

    ComposeUI("androidx.compose.ui", "ui", Version.Compose),
    ComposeUIToolkit("androidx.compose.ui", "ui-tooling", Version.Compose),
    ComposeFoundation("androidx.compose.foundation", "foundation", Version.ComposeFoundation),
    ComposeMaterial("androidx.compose.material3", "material3", Version.ComposeMaterial3),
    ComposeMaterialWindowSize("androidx.compose.material3", "material3-window-size-class", Version.ComposeMaterial3),
    ActivityCompose("androidx.activity", "activity-compose", Version.ActivityCompose),

    CoilCompose("io.coil-kt", "coil-compose", Version.CoilCompose),

    DebugComposeUiToolingPreview("androidx.compose.ui", "ui-tooling-preview", Version.Compose),

    AccompanistPermission("com.google.accompanist", "accompanist-permissions", Version.Accompanist),

    Decompose("com.arkivanov.decompose", "decompose", Version.Decompose),
    DecomposeExtensions("com.arkivanov.decompose", "extensions-compose-jetpack", Version.Decompose),

    // TODO compose preview not working, check issue: https://issuetracker.google.com/issues/227767363
    DebugComposeCustomView("androidx.customview", "customview", Version.DebugComposeCustomView),
    DebugComposeCustomViewPoolingcontainer(
        "androidx.customview",
        "customview-poolingcontainer",
        Version.DebugComposeCustomViewPoolingcontainer
    ),
    ;

    val notation = "$group:$artifact:${version.value}"

    internal enum class Version(val value: String) {
        ActivityCompose("1.8.2"),
        AndroidXAppcompat("1.6.1"),
        AndroidXCore("1.12.0"),
        AndroidXAnnotation("1.7.1"),
        AndroidXSplash("1.0.0"),
        Kotlin("1.9.22"),
        Coroutines("1.8.0"),
        SQLDelight("2.0.1"),
        Leakcanary("2.13"),
        Compose("1.6.1"), /*MUST BE CHANGED WITH ACCOMPANIST VERSION*/
        ComposeFoundation("1.6.1"), /*MUST BE CHANGED WITH ACCOMPANIST VERSION*/
        ComposeMaterial3("1.2.0"),
        Accompanist("0.34.0") /*MUST BE CHANGED WITH COMPOSE VERSION*/,
        CoilCompose("2.5.0"),
        Decompose("2.2.2"),

        DebugComposeCustomView("1.2.0-alpha02"),
        DebugComposeCustomViewPoolingcontainer("1.0.0"),
    }
}

enum class Toolkit(
    internal val libs: List<Library> = emptyList(),
    internal val projects: List<Project> = emptyList()
) {
    Compose(
        listOf(
            Library.ComposeUI,
            Library.ComposeUIToolkit,
            Library.ComposeFoundation,
            Library.ComposeMaterial,
            Library.ComposeMaterialWindowSize,
            Library.CoilCompose,
            Library.ActivityCompose
        )
    ),
    Decompose(
        listOf(
            Library.Decompose,
            Library.DecomposeExtensions
        )
    ),
    UI(
        libs = listOf(
            Library.ComposeUI,
            Library.ComposeUIToolkit,
            Library.ComposeFoundation,
            Library.ComposeMaterial,
            Library.ComposeMaterialWindowSize,
            Library.CoilCompose,
            Library.ActivityCompose,
            Library.Decompose,
            Library.DecomposeExtensions
        ),
        projects = listOf(
            Project.Tools.Presentation.Compose,
            Project.Tools.Presentation.Decompose,
        )
    )
}

enum class FirebaseLibrary(group: String, artifact: String) {
    FirebaseCrashLytics("com.google.firebase", "firebase-crashlytics-ktx"),
    FirebaseAnalytics("com.google.firebase", "firebase-analytics-ktx")
    ;

    val notation = "$group:$artifact"

    companion object {
        val bom = "com.google.firebase:firebase-bom:32.7.2"
    }
}

enum class Plugin(group: String, artifact: String, version: Version) {
    Android("com.android.tools.build", "gradle", Version.Android),
    Kotlin("org.jetbrains.kotlin", "kotlin-gradle-plugin", Version.Kotlin),
    Google("com.google.gms", "google-services", Version.Google),
    Crashlytics("com.google.firebase", "firebase-crashlytics-gradle", Version.Crashlytics),
    Versions("com.github.ben-manes", "gradle-versions-plugin", Version.Versions),
    SQLDelight("app.cash.sqldelight", "gradle-plugin", Version.SQLDelight),
    GooglePlayPublisher("com.github.triplet.gradle", "play-publisher", Version.GooglePlayPublisher),
    FirebasePublisher("com.google.firebase", "firebase-appdistribution-gradle", Version.FirebasePublisher)
    ;

    internal val notation = "$group:$artifact:${version.value}"

    enum class Id(val value: String) {
        AndroidApplication("com.android.application"),
        AndroidLibrary("com.android.library"),
        KotlinAndroid("org.jetbrains.kotlin.android"),
        KotlinParcelize("kotlin-parcelize"),
        KotlinJvm("org.jetbrains.kotlin.jvm"),
        JavaLibrary("java-library"),
        GoogleServices("com.google.gms.google-services"),
        Crashlytics("com.google.firebase.crashlytics"),
        Versions("com.github.ben-manes.versions"),
        SQLDelight("app.cash.sqldelight"),
        GooglePlayPublisher("com.github.triplet.play"),
        FirebasePublisher("com.google.firebase.appdistribution")
    }

    private enum class Version(val value: String) {
        Android("8.2.2"),
        Kotlin(Library.Version.Kotlin.value),
        Google("4.3.13"),
        Crashlytics("2.9.1"),
        Versions("0.51.0"),
        SQLDelight(Library.Version.SQLDelight.value),
        GooglePlayPublisher("3.8.4"),
        FirebasePublisher("4.0.0"),
    }
}

const val KotlinCompilerExtensionVersion = "1.5.9" /*must be synchronized with kotlin and agp version*/

enum class Tools(val version: String) {
    Build("34.0.0"),
}

sealed class Project(id: String) {
    val name: String = ":$id"

    sealed class Tools(id: String) : Project("tools:$id") {
        sealed class Presentation(id: String) : Tools("presentation:$id") {
            object Compose : Presentation("compose")
            object Decompose : Presentation("decompose")
        }

        object Entity : Tools("entity")
        object Coroutines : Tools("coroutines")

        sealed class Platform(id: String) : Tools("platform:$id") {
            object Utils : Platform("utils")
        }
    }

    sealed class Logger(id: String) : Project("logger:$id") {
        object Core : Logger("core")
        object Crashlytics : Logger("crashlytics")
        object Logcat : Logger("logcat")
        object InternalStore : Logger("internal-store")
    }
}

fun DependencyHandler.plugin(plugin: Plugin) = add(ScriptHandler.CLASSPATH_CONFIGURATION, plugin.notation)

fun DependencyHandler.implementation(toolkit: Toolkit) {
    toolkit.libs.forEach(::implementation)
    toolkit.projects.forEach(::implementation)
}

fun DependencyHandler.implementation(library: Library) = add("implementation", library.notation)
fun DependencyHandler.debugImplementation(library: Library) = add("debugImplementation", library.notation)
fun DependencyHandler.implementation(vararg firebaseLibrary: FirebaseLibrary) {
    add("implementation", platform(FirebaseLibrary.bom))
    firebaseLibrary.forEach { lib -> add("implementation", lib.notation) }
}

fun DependencyHandler.implementation(project: Project) = add("implementation", project(project.name))
fun DependencyHandler.debugImplementation(project: Project) = add("debugImplementation", project(project.name))
fun DependencyHandler.api(project: Project) = add("api", project(project.name))
fun DependencyHandler.api(library: Library) = add("api", library.notation)
fun DependencyHandler.debugApi(library: Library) = add("debugApi", library.notation)
