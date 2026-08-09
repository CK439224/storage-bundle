import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension

/**
 * Enables Jetpack Compose and adds the BOM-managed UI dependencies.
 *
 * Applied on top of either [storage.android.library] or [storage.android.application]. Because
 * this plugin does not itself apply an Android plugin, it reacts to whichever one is present
 * rather than assuming an `android { }` accessor exists.
 *
 * The Compose compiler ships with the Kotlin plugin, so there is no separate compiler version
 * to keep in sync.
 */
plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

pluginManager.withPlugin("com.android.library") {
    extensions.configure<LibraryExtension>("android") {
        buildFeatures.compose = true
    }
}

pluginManager.withPlugin("com.android.application") {
    extensions.configure<ApplicationExtension>("android") {
        buildFeatures.compose = true
    }
}

dependencies {
    val bom = libs.lib("androidx-compose-bom")
    add("implementation", platform(bom))
    add("androidTestImplementation", platform(bom))

    add("implementation", libs.lib("androidx-compose-ui"))
    add("implementation", libs.lib("androidx-compose-ui-graphics"))
    add("implementation", libs.lib("androidx-compose-ui-tooling-preview"))
    add("implementation", libs.lib("androidx-compose-material3"))
    add("implementation", libs.lib("androidx-lifecycle-runtime-compose"))
    add("implementation", libs.lib("androidx-lifecycle-viewmodel-compose"))

    add("debugImplementation", libs.lib("androidx-compose-ui-tooling"))
    add("debugImplementation", libs.lib("androidx-compose-ui-test-manifest"))

    add("androidTestImplementation", libs.lib("androidx-compose-ui-test-junit4"))
}
