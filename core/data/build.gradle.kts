plugins {
    alias(libs.plugins.storage.android.library)
    alias(libs.plugins.storage.android.hilt)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.storagebundle.core.data"
}

room {
    // Schemas are committed so migrations can be diffed in review and tested against
    // real historical schemas (PLAN.md §8).
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(projects.core.common)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit4)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
