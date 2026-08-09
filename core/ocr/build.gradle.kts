plugins {
    alias(libs.plugins.storage.android.library)
    alias(libs.plugins.storage.android.hilt)
}

android {
    namespace = "com.storagebundle.core.ocr"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.data)

    // Bundled model, not the play-services variant — the app must work on GMS-free
    // devices, which is also an F-Droid requirement (PLAN.md §3).
    implementation(libs.mlkit.text.recognition)

    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit4)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
