// v0.2 (PLAN.md §5.2). Skeleton only in Phase 0.
plugins {
    alias(libs.plugins.storage.android.feature)
}

android {
    namespace = "com.storagebundle.feature.dupes"
}

dependencies {
    implementation(projects.core.media)
    implementation(projects.core.hashing)
}
