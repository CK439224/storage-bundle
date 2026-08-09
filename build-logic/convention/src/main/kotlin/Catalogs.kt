import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

/**
 * Version-catalog access for precompiled script plugins.
 *
 * Gradle does not generate typed `libs.*` accessors inside `build-logic`, so convention plugins
 * look entries up by name instead. Keeping that lookup here means a typo surfaces in one place
 * rather than being repeated across every plugin.
 */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/** Returns the required version for [name], failing the build if the alias is missing. */
internal fun VersionCatalog.version(name: String): String =
    findVersion(name)
        .orElseThrow { IllegalStateException("Version '$name' missing from libs.versions.toml") }
        .requiredVersion

/** Returns [version] parsed as an Int — used for SDK levels. */
internal fun VersionCatalog.intVersion(name: String): Int =
    version(name).toIntOrNull()
        ?: error("Version '$name' in libs.versions.toml is not an integer")

/** Returns the library coordinate for [name], failing the build if the alias is missing. */
internal fun VersionCatalog.lib(name: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(name)
        .orElseThrow { IllegalStateException("Library '$name' missing from libs.versions.toml") }
