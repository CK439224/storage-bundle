// v0.1 lead feature (PLAN.md §5.1). Screens land here in the next phase;
// Phase 0 establishes the module and its wiring only.
plugins {
    alias(libs.plugins.storage.android.feature)
}

android {
    namespace = "com.storagebundle.feature.screenshots"
}

dependencies {
    implementation(projects.core.media)
    implementation(projects.core.ocr)
    implementation(projects.core.hashing)
}
