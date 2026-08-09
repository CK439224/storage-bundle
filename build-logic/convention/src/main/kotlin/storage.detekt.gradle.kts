import io.gitlab.arturbosch.detekt.Detekt

/**
 * Static analysis for every module (PLAN.md §7).
 *
 * `detekt-formatting` embeds the ktlint rule set, so a single tool enforces both detekt's own
 * rules and official Kotlin style. Standards that are not automated do not survive contact
 * with a deadline.
 */
plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("${rootProject.rootDir}/config/detekt/detekt.yml"))
    baseline = file("${rootProject.rootDir}/config/detekt/baseline.xml").takeIf { it.exists() }
    parallel = true
}

dependencies {
    add("detektPlugins", libs.lib("detekt-formatting"))
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "17"
    reports {
        html.required.set(true)
        xml.required.set(true)
        sarif.required.set(true)
        txt.required.set(false)
        md.required.set(false)
    }
}
