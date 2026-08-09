package com.storagebundle.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Fails the build if the app declares any network-capable permission.
 *
 * "This app has no network access" is a public, user-verifiable marketing claim (PLAN.md §1.3),
 * which makes it an architectural invariant rather than a preference. A claim users can check is
 * a claim that must not silently break.
 *
 * This inspects the **merged** manifest rather than the hand-written source manifest, because the
 * realistic failure mode is a transitive dependency contributing `<uses-permission>` through the
 * manifest merger — something a source-file scan would not catch.
 */
@CacheableTask
abstract class NoNetworkPermissionCheckTask : DefaultTask() {

    /** The post-merge manifest for a single variant. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val mergedManifest: RegularFileProperty

    /** Marker file so Gradle can treat this check as up-to-date. */
    @get:OutputFile
    abstract val stampFile: RegularFileProperty

    @TaskAction
    fun verify() {
        val manifest = mergedManifest.get().asFile
        val offenders = declaredPermissions(manifest).filter { it in BANNED_PERMISSIONS }.sorted()

        if (offenders.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("Network permission found in the merged manifest.")
                    appendLine()
                    offenders.forEach { appendLine("  - $it") }
                    appendLine()
                    appendLine("The app promises no network access (PLAN.md §1.3) and declares no")
                    appendLine("INTERNET permission. If a dependency introduced this, remove the")
                    appendLine("dependency or strip the permission with tools:node=\"remove\".")
                    appendLine()
                    appendLine("Merged manifest: ${manifest.absolutePath}")
                },
            )
        }

        stampFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText("No network permissions present.\n")
        }
    }

    /**
     * Returns the `android:name` of every `<uses-permission>` element in [manifest].
     *
     * Parses the XML rather than scanning for substrings: the merged manifest keeps source
     * comments, so a naive text search matches this project's own documentation of the
     * permissions it deliberately does *not* declare.
     */
    private fun declaredPermissions(manifest: File): List<String> {
        val document = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(manifest)

        val nodes = document.getElementsByTagName("uses-permission")
        return (0 until nodes.length).mapNotNull { index ->
            val element = nodes.item(index) as? Element
            element?.getAttributeNS(ANDROID_NAMESPACE, "name")?.takeIf { it.isNotEmpty() }
        }
    }

    private companion object {
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"

        /**
         * Permissions that would grant, or strongly imply, network reachability.
         * ACCESS_NETWORK_STATE and ACCESS_WIFI_STATE cannot themselves move data, but their
         * presence means something in the tree expects to.
         */
        val BANNED_PERMISSIONS = listOf(
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.ACCESS_WIFI_STATE",
            "android.permission.CHANGE_NETWORK_STATE",
            "android.permission.CHANGE_WIFI_STATE",
        )
    }
}
