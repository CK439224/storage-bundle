import java.util.Properties

plugins {
    alias(libs.plugins.storage.android.application)
    alias(libs.plugins.storage.android.compose)
    alias(libs.plugins.storage.android.hilt)
}

/**
 * Release signing is configured only when `keystore.properties` is present, so a plain
 * `git clone` still builds. CI materialises that file from repository secrets; it is
 * git-ignored and must never be committed (PLAN.md §9).
 */
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use(::load)
    }
}

android {
    namespace = "com.storagebundle"

    defaultConfig {
        applicationId = "com.storagebundle"

        // See VERSIONING.md — versionName is the source of truth and must match the
        // release tag; versionCode increases monotonically and never resets.
        versionCode = 1
        versionName = "0.1.0-alpha01"
    }

    androidResources {
        // Ship only the locales we actually translate, keeping the APK small.
        localeFilters += listOf("en")
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.findByName("release")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    // Reproducible builds are an F-Droid prerequisite (PLAN.md §9).
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.media)

    implementation(projects.feature.screenshots)
    implementation(projects.feature.dupes)
    implementation(projects.feature.permissions)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.kotlinx.coroutines.android)

    testImplementation(projects.core.testing)
    testImplementation(libs.junit4)
    testImplementation(libs.robolectric)

    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
