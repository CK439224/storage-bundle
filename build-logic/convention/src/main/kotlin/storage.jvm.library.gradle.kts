import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Pure Kotlin/JVM module — no Android framework dependency.
 *
 * Used by `:core:hashing`, deliberately: the similarity algorithm is the riskiest correctness
 * surface in the app, and keeping it Android-free means it is covered by fast JVM unit tests
 * with no emulator (PLAN.md §4).
 */
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("storage.detekt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        allWarningsAsErrors.set(providers.gradleProperty("warningsAsErrors").map { it.toBoolean() }.orElse(false))
    }
}

tasks.withType<Test>().configureEach {
    useJUnit()
}

dependencies {
    add("testImplementation", libs.lib("junit4"))
    add("testImplementation", libs.lib("kotlinx-coroutines-test"))
}
