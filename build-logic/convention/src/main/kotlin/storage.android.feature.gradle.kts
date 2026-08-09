/**
 * The composite plugin every `:feature:*` module applies.
 *
 * A feature module is an Android library with Compose, Hilt, navigation, and the shared
 * `:core` modules already on its classpath — so a new feature's build file is one line
 * (PLAN.md §4: v0.2 and v0.3 fold in without restructuring).
 */
plugins {
    id("storage.android.library")
    id("storage.android.compose")
    id("storage.android.hilt")
}

dependencies {
    add("implementation", project(":core:ui"))
    add("implementation", project(":core:common"))
    add("implementation", project(":core:data"))

    add("implementation", libs.lib("androidx-hilt-navigation-compose"))
    add("implementation", libs.lib("androidx-navigation-compose"))
    add("implementation", libs.lib("kotlinx-coroutines-android"))

    add("testImplementation", project(":core:testing"))
    add("testImplementation", libs.lib("junit4"))
    add("testImplementation", libs.lib("mockk"))
    add("testImplementation", libs.lib("turbine"))
    add("testImplementation", libs.lib("kotlinx-coroutines-test"))
}
