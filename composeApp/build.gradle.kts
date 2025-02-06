import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.UUID

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

val ktorVersion = "3.0.3"

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
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("io.ktor:ktor-client-cio:$ktorVersion")
            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
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
            packageName = "Chafenqi Updater"
            packageVersion = "1.0.1"
            vendor = "NLTV"

            windows {
                dirChooser = true
                shortcut = true
                iconFile.set(project.file("src/commonMain/composeResources/drawable/app_icon.ico"))
                upgradeUuid = generateUUID(packageName ?: "Chafenqi Updater", packageVersion ?: "1.0.1")
                menuGroup = "Chafenqi"
            }
        }

        buildTypes.release.proguard {
            configurationFiles.from(project.file("proguard-rules.pro"))
        }
    }
}

fun generateUUID(
    packageName: String,
    packageVersion: String,
) = UUID.nameUUIDFromBytes((packageName + packageVersion).toByteArray()).toString().uppercase()
