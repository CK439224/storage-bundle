plugins {
    alias(libs.plugins.storage.android.library)
    alias(libs.plugins.storage.android.hilt)
}

android {
    namespace = "com.storagebundle.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
