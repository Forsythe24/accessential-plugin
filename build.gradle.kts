import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.3.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
}

group = "com.balsdon"
version = System.getenv("VERSION_NUMBER")

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

configurations { create("externalLibs") }

java {
    sourceCompatibility = JavaVersion.VERSION_20
    targetCompatibility = JavaVersion.VERSION_20
}

intellijPlatform {
    pluginConfiguration {
        name = "accessential"
        group = "com.solopov.accessential-plugin"
        ideaVersion.sinceBuild.set(project.property("sinceBuild").toString())
        ideaVersion.untilBuild.set(provider { null })
    }
    buildSearchableOptions.set(false)
    instrumentCode = true

    signing {
        privateKey = System.getenv("PRIVATE_KEY")
        password = System.getenv("PRIVATE_KEY_PASSWORD")
        certificateChain = System.getenv("CERTIFICATE_CHAIN")
    }

    publishing {
        token = System.getenv("PUBLISH_TOKEN")
    }
}

dependencies {
    intellijPlatform {
        bundledPlugin("org.jetbrains.android")
        testFramework(TestFrameworkType.Platform)
        zipSigner()
        if (project.hasProperty("localIdeOverride")) {
            local(property("localIdeOverride").toString())
        } else {
            androidStudio(property("ideVersion").toString())
        }
    }

    val rxJava = "3.1.8"
    implementation("io.reactivex.rxjava3:rxjava:$rxJava")
    val zipSigner = "0.1.8"
    implementation(dependencyNotation = "org.jetbrains:marketplace-zip-signer:$zipSigner")
    val googleTruth = "1.1.4"
    testImplementation("com.google.truth:truth:$googleTruth")

    testImplementation("junit:junit:4.13.2")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/config/detekt.yaml")
    baseline = file("$projectDir/config/baseline.xml")
}

tasks {
    val projectJvmTarget = "20"

    withType<Detekt>().configureEach {
        reports {
            html.required.set(true)
            xml.required.set(true)
            txt.required.set(true)
            sarif.required.set(true)
            md.required.set(true)
        }
    }

    withType<Detekt>().configureEach {
        jvmTarget = "1.8"
    }
    withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget = "1.8"
    }

    buildSearchableOptions {
        enabled = false
    }

    withType<JavaCompile> {
        sourceCompatibility = projectJvmTarget
        targetCompatibility = projectJvmTarget
    }

    runIde {
        jvmArgs = listOf("-Xmx4096m", "-XX:+UnlockDiagnosticVMOptions")
    }
}