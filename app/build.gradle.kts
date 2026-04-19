import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
}

kotlin {
    jvm("desktop")
    jvmToolchain(21)

    sourceSets {
        val desktopMain by getting {
            dependencies {
//                implementation(libs.compose.ui)
//                implementation(libs.compose.foundation)
//                implementation(libs.compose.material3)
//                implementation(libs.compose.material.icons.extended)
//                implementation(libs.compose.components.resources)

                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.materialIconsExtended)
                implementation(libs.bundletool)
                implementation(libs.moshi.kotlin)
                implementation(libs.moshi.kotlin.codegen)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
    }
}

dependencies {
    add("kspDesktop", libs.moshi.kotlin.codegen)
}

compose.desktop {
    application {
        mainClass = "mrsohn.project.aabtools.MainKt"
        // jpackage가 포함된 JDK 경로를 직접 지정
        javaHome = "/Users/okpos/.gradle/jdks/eclipse_adoptium-21-aarch64-os_x.2/jdk-21.0.9+10/Contents/Home"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "AABTools"
            packageVersion = "1.0.0"
        }
    }
}
