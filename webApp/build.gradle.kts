import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets.commonMain.dependencies {
        implementation(projects.shared)
        implementation(compose.ui)
    }

    sourceSets.wasmJsMain.dependencies {
        implementation(devNpm("webpack", "5.108.1"))
        implementation(devNpm("webpack-cli", "7.2.1"))
    }
}
