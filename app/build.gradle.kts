import com.android.build.gradle.internal.signing.SigningConfigData
import com.android.build.gradle.internal.signing.SigningConfigProvider
import org.jetbrains.kotlin.konan.properties.loadProperties

/*
 * Sub-licenses:
 *         https://github.com/google/material-design-icons/blob/master/LICENSE
 *         https://github.com/Templarian/MaterialDesign/blob/master/LICENSE
 *         https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt
 * This project:
 *         Copyright (C) 2019 Penn Mackintosh
 *         Licensed under https://www.gnu.org/licenses/gpl-3.0.en.html
 */

plugins {
    id("com.gladed.androidgitversion") version "0.4.13"
    id("com.android.application")
    kotlin("android")
    id("poeditor-android")
}

val kotlinVersion = rootProject.extra["kotlinVersion"]

fun com.android.build.gradle.internal.dsl.BuildType.setupBilling() {
    if (project.properties["gplayDebug"] == true || !file("billing.properties").exists()) {
        buildConfigField("boolean", "DONATIONS_DEBUG", "true")
        buildConfigField("String", "GPLAY_PUBK", "\"\"")
        buildConfigField("String[]", "GPLAY_KEYS", "{}")
        buildConfigField("int[]", "GPLAY_VALS", "{}")
        buildConfigField("String", "PAYPAL_EMAIL", "\"example@example.com\"")
        buildConfigField("String", "PAYPAL_CURRENCY", "\"USD\"")
        buildConfigField("String", "PAYPAL_DESCRIPTION", "\"Testing!\"")
    } else {
        loadProperties(file("billing.properties").absolutePath).run {
            buildConfigField("boolean", "DONATIONS_DEBUG", "false")
            buildConfigField("String", "GPLAY_PUBK", getProperty("gplayPubk"))
            buildConfigField("String[]", "GPLAY_KEYS", getProperty("gplayKeys"))
            buildConfigField("int[]", "GPLAY_VALS", getProperty("gplayVals"))
            buildConfigField("String", "PAYPAL_EMAIL", getProperty("paypalEmail"))
            buildConfigField("String", "PAYPAL_CURRENCY", getProperty("paypalCurrency"))
            buildConfigField("String", "PAYPAL_DESCRIPTION", getProperty("paypalDescription"))
        }
    }
}

fun getPassword(prompt: String): String? = run {
    System.console()?.let {
        return@run it.readPassword("prompt")?.contentToString() ?: return@let
    }
    val process = ProcessBuilder("zenity", "--password", "--title", prompt).start()
    process.waitFor()
    if (process.exitValue() != 0)
        return null
    return@run process.inputStream.reader().readText()
}.dropLast(1) // newline

android {
    compileSdk = 30
    defaultConfig {
        applicationId = "tk.hack5.treblecheck"
        minSdk = 22
        targetSdk = 30
        versionCode = androidGitVersion.code()
        versionName = androidGitVersion.name()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    if (file("signing.properties").exists()) {
        loadProperties(file("signing.properties").absolutePath).run {
            signingConfigs {
                create("release") {
                    keyAlias = getProperty("keyAlias")
                    storeFile = file(getProperty("storeFile"))
                    // without these AGP assumes that the signing will fail before it even starts
                    keyPassword = ""
                    storePassword = ""
                }
            }
        }
    }

    buildTypes {
        getByName("release") {
            minifyEnabled(true)
            isShrinkResources = true
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), file("proguard-rules.pro")))
            signingConfig = signingConfigs["release"]

            if (!project.hasProperty("gplayDebug"))
                project.ext.set("gplayDebug", false)
            setupBilling()
        }
        getByName("debug") {
            signingConfig = signingConfigs["debug"]
            if (!project.hasProperty("gplayDebug"))
                project.ext.set("gplayDebug", true)
            setupBilling()
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

task("inputKeyPasswords") {
    doFirst {
        getPassword("Store Password")?.let {
            android.signingConfigs.getByName("release") {
                storePassword = it
                keyPassword = getPassword("Key Password")
            }

            tasks.filterIsInstance<com.android.build.gradle.tasks.PackageApplication>().forEach {
                val config = android.buildTypes[(it.signingConfig.signingConfigData ?: return@forEach).name].signingConfig ?: return@forEach
                it.signingConfig = SigningConfigProvider(
                    SigningConfigData.fromSigningConfig(config),
                    it.signingConfig.signingConfigFileCollection,
                    it.signingConfig.signingConfigValidationResultDir
                )
            }
        }
    }
    afterEvaluate {
        tasks.getByName("validateSigningRelease").dependsOn(this@task)
    }
}

if (file("poeditor.properties").exists()) {
    project.poeditor.apiToken = loadProperties(file("poeditor.properties").absolutePath).getProperty("apiToken")
}

project.poeditor.projectId = 285385


dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("com.google.android.material:material:1.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    androidTestImplementation("tools.fastlane:screengrab:2.1.1") // requires github.com/penn5/fastlane, provided via mavenLocal
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    implementation("com.github.penn5:donations:3.5.1")
}
