// Plugin versions are declared once here so the convention plugins in build-logic/ can
// apply them by id without repeating a version. Nothing is applied at the root.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.detekt) apply false
}

/**
 * Aggregate entry point for CI (PLAN.md §9) — one task that runs every static-analysis
 * and correctness gate across all modules.
 */
tasks.register("verifyAll") {
    group = "verification"
    description = "Runs detekt, lint, unit tests and the no-network manifest gate for every module."
    // `:core` and `:feature` are grouping containers with no build file and therefore no
    // `check` task — only real modules are included.
    dependsOn(
        subprojects.filter { it.buildFile.exists() }.map { "${it.path}:check" },
    )
}
