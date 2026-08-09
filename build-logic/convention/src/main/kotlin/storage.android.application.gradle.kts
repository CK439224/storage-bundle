import com.android.build.api.artifact.SingleArtifact
import com.storagebundle.buildlogic.NoNetworkPermissionCheckTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Baseline configuration for the single application module.
 */
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("storage.detekt")
}

android {
    compileSdk = libs.intVersion("compileSdk")

    defaultConfig {
        minSdk = libs.intVersion("minSdk")
        targetSdk = libs.intVersion("targetSdk")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        allWarningsAsErrors.set(providers.gradleProperty("warningsAsErrors").map { it.toBoolean() }.orElse(false))
    }
}

// Register the no-network gate per variant and hook it into `check` so CI and local
// builds both enforce it (PLAN.md §1.3, §9).
androidComponents {
    onVariants { variant ->
        val capitalisedName = variant.name.replaceFirstChar { it.uppercaseChar() }
        val verifyTask = tasks.register<NoNetworkPermissionCheckTask>(
            "check${capitalisedName}NoNetworkPermission",
        ) {
            group = "verification"
            description = "Fails if variant '${variant.name}' declares a network permission."
            mergedManifest.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            stampFile.set(layout.buildDirectory.file("reports/no-network/${variant.name}.txt"))
        }

        tasks.named("check") {
            dependsOn(verifyTask)
        }
    }
}
