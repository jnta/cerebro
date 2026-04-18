plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    kotlin("multiplatform").apply(false)
    id("org.jetbrains.compose").apply(false)
    kotlin("plugin.compose").version("2.1.20").apply(false)
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        buildUponDefaultConfig = true
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    }
}
