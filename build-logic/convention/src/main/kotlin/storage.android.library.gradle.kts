import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Baseline configuration for every Android library module.
 *
 * Keeps SDK levels, Java/Kotlin targets, and test options identical across the eleven modules
 * so they cannot drift (PLAN.md §4).
 */
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("storage.detekt")
}

android {
    compileSdk = libs.intVersion("compileSdk")

    defaultConfig {
        minSdk = libs.intVersion("minSdk")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    // No BuildConfig in libraries — nothing here needs it, and it slows the build.
    buildFeatures {
        buildConfig = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        // Warnings are build failures in CI (PLAN.md §7).
        allWarningsAsErrors.set(providers.gradleProperty("warningsAsErrors").map { it.toBoolean() }.orElse(false))
    }
}
