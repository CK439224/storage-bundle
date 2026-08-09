// v0.3 (PLAN.md §5.3). Deliberately shares no code with the media stack, so it can be
// built in parallel at any point. Skeleton only in Phase 0.
plugins {
    alias(libs.plugins.storage.android.feature)
}

android {
    namespace = "com.storagebundle.feature.permissions"
}
