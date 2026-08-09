/**
 * Wires Hilt dependency injection into an Android module (PLAN.md §4).
 *
 * KSP rather than kapt — it is materially faster and is the supported path on Kotlin 2.x.
 */
plugins {
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

dependencies {
    add("implementation", libs.lib("hilt-android"))
    add("ksp", libs.lib("hilt-compiler"))

    add("testImplementation", libs.lib("hilt-android-testing"))
    add("kspTest", libs.lib("hilt-compiler"))
    add("androidTestImplementation", libs.lib("hilt-android-testing"))
    add("kspAndroidTest", libs.lib("hilt-compiler"))
}
