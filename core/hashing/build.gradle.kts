// Pure Kotlin/JVM by design — no Android dependency, so the similarity algorithm
// is covered by fast unit tests with no emulator (PLAN.md §4).
plugins {
    alias(libs.plugins.storage.jvm.library)
}
