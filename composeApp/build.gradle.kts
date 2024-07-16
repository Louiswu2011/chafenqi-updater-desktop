import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.JvmApplication
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

val ktor_version = "2.3.12"

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
            implementation("androidx.datastore:datastore:1.1.1")
            implementation("androidx.datastore:datastore-preferences:1.1.1")
            implementation("androidx.datastore:datastore-preferences-core:1.1.1")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose("org.jetbrains.kotlinx:kotlinx-coroutines-swing"))
            implementation("io.ktor:ktor-client-core:$ktor_version")
            implementation("io.ktor:ktor-client-cio:$ktor_version")
            implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
            implementation("org.slf4j:slf4j-api:2.0.9")
            implementation("org.slf4j:slf4j-simple:2.0.9")
            implementation("io.github.dokar3:sonner:0.3.8")
        }
    }
}


compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb)
            modules("java.instrument", "java.management", "jdk.unsupported")
            packageName = "ChafenqiUpdater"
            packageVersion = "1.0.0"

            windows {
                dirChooser = true
                iconFile.set(project.file("src/commonMain/composeResources/drawable/app_icon.png"))
            }
        }

        buildTypes.release.proguard {
            configurationFiles.from(project.file("proguard-rules.pro"))
        }
    }
}
