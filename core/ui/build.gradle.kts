plugins {
    alias(libs.plugins.storage.android.library)
    alias(libs.plugins.storage.android.compose)
}

android {
    namespace = "com.storagebundle.core.ui"
}

dependencies {
    implementation(projects.core.common)

    api(libs.androidx.compose.material.icons.extended)
    api(libs.coil.compose)

    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit4)
}
