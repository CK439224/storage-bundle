plugins {
    alias(libs.plugins.storage.android.library)
    alias(libs.plugins.storage.android.hilt)
}

android {
    namespace = "com.storagebundle.core.media"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.hashing)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.exifinterface)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit4)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
