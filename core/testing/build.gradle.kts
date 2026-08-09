plugins {
    alias(libs.plugins.storage.android.library)
}

android {
    namespace = "com.storagebundle.core.testing"
}

dependencies {
    // Test fixtures are `api` so consuming modules get them transitively.
    api(libs.junit4)
    api(libs.mockk)
    api(libs.turbine)
    api(libs.kotlinx.coroutines.test)
    api(libs.androidx.test.core)

    implementation(projects.core.common)
    implementation(libs.kotlinx.coroutines.android)
}
