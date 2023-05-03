import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> when (System.getProperty("os.arch")) {
            "aarch64" -> macosArm64("native")
            else -> macosX64("native")
        }
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    targets.configureEach {
        compilations.configureEach {
            kotlinOptions {
                freeCompilerArgs += listOf("-Xcontext-receivers")
            }
        }
    }

    nativeTarget.apply {
        binaries {
            executable {
                baseName = "skynet"
                entryPoint = "main"
            }
        }
    }
    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation(projects.lib)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization)
                implementation(libs.okio)
                implementation(libs.result)
                implementation(libs.uuid)
            }
        }
        val nativeTest by getting
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.set(listOf())
    }
}

tasks.register<Exec>("integrationTest") {
    dependsOn(tasks.named("linkDebugExecutableNative"))

    val node = project.findProperty("node") as String? ?: "echo"

    val requestDir = "../messages"
    val init = "$requestDir/init.json"
    val json = "$requestDir/$node.json"
    val bin = "./build/bin/native/debugExecutable/skynet*"

    executable = "bash"
    args = listOf("-c", "cat $init $json | $bin")
}

tasks.register("skynet") {
    dependsOn(tasks.named("runDebugExecutableNative"))
}
