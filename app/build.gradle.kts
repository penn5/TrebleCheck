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
    id("com.gladed.androidgitversion") version "0.4.14"
    id("com.android.application")
    kotlin("android")
    id("poeditor-android")
}

val kotlinVersion = rootProject.extra["kotlinVersion"]

fun com.android.build.api.dsl.BuildType.setupBilling(debugByDefault: Boolean) {
    if (project.properties["gplayDebug"] as Boolean? ?: debugByDefault || !file("billing.properties").exists()) {
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

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "tk.hack5.treblecheck"
        minSdk = 22
        targetSdk = 31
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
                    keyPassword = getProperty("keyPassword")
                    storePassword = getProperty("storePassword")
                }
            }
        }
    }

    buildTypes {
        if (file("signing.properties").exists()) {
            getByName("release") {
                isMinifyEnabled = true
                isShrinkResources = true
                setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), file("proguard-rules.pro")))
                signingConfig = signingConfigs["release"]

                setupBilling(false)
            }
        }
        getByName("debug") {
            signingConfig = signingConfigs["debug"]
            setupBilling(true)
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

    packagingOptions {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

if (file("poeditor.properties").exists()) {
    project.poeditor.apiToken = loadProperties(file("poeditor.properties").absolutePath).getProperty("apiToken")
}

project.poeditor.projectId = 285385


dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("com.google.android.material:material:1.5.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    androidTestImplementation("tools.fastlane:screengrab:2.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    implementation("com.github.penn5:donations:3.6.0")
}
