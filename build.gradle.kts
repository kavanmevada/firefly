plugins {
    kotlin("multiplatform") version "1.4.0-dev-4220"
}

group = "me.kavan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-dev") }
}

kotlin {
    val nativeTarget = when (System.getProperty("os.name")) {
        "Mac OS X" -> macosX64("native")
        "Linux" -> linuxX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries.executable {
            entryPoint = "main"
        }
    }

    //sourceSets { nativeMain by getting }
}