/*
 *     Treble Info
 *     Copyright (C) 2023 Hackintosh Five
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import com.android.build.gradle.tasks.MergeResources
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
    id("materialdesignicons-android")
    id("com.mikepenz.aboutlibraries.plugin") version "10.5.2"
}

aboutLibraries {
    configPath = projectDir.resolve("librariesConfig").toString()
}

val kotlinVersion = rootProject.extra["kotlinVersion"]
val mockkVersion = "1.12.3"

fun com.android.build.api.dsl.BuildType.setupBilling(debugByDefault: Boolean) {
    buildConfigField("String", "GPLAY_PRODUCT", "\"donate_4\"") // TODO

    buildConfigField("String", "PAYPAL_EMAIL", "\"example@example.com\"")
    buildConfigField("String", "PAYPAL_CURRENCY", "\"USD\"")
    buildConfigField("String", "PAYPAL_DESCRIPTION", "\"Testing!\"")
    /*if (project.properties["gplayDebug"] as Boolean? ?: debugByDefault || !file("billing.properties").exists()) {
        buildConfigField("boolean", "DONATIONS_DEBUG", "true")
        buildConfigField("String", "GPLAY_PRODUCT", "\"android.test.purchased\"")
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
    }*/
}

android {
    compileSdk = 33
    buildToolsVersion = "33.0.0"
    defaultConfig {
        applicationId = "tk.hack5.treblecheck"
        minSdk = 22
        targetSdk = 33
        versionCode = androidGitVersion.code()
        versionName = androidGitVersion.name()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    flavorDimensions += "freedom"
    productFlavors {
        create("free") {
            dimension = "freedom"
        }
        create("nonfree") {
            dimension = "freedom"
        }
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
        getByName("release") {
            if (file("signing.properties").exists()) {
                signingConfig = signingConfigs["release"]
                setupBilling(false)
            } else {
                setupBilling(true)
            }
            isMinifyEnabled = false // TODO true
            isShrinkResources = false // TODO true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        compose = true
    }

    packagingOptions {
        jniLibs {
            useLegacyPackaging = false
        }
        resources {
            excludes.add("DebugProbesKt.bin")
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        checkDependencies = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["composeVersion"] as String
    }
    namespace = "tk.hack5.treblecheck"
}

if (file("poeditor.properties").exists()) {
    project.poeditor.apiToken = loadProperties(file("poeditor.properties").absolutePath).getProperty("apiToken")
}

project.poeditor.projectId = 285385


dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("com.google.android.material:material:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.compose.ui:ui:1.3.3")
    implementation("androidx.compose.material3:material3:1.1.0-alpha04")
    implementation("androidx.compose.material3:material3-window-size-class:1.1.0-alpha04")
    implementation("androidx.compose.ui:ui-tooling-preview:1.3.3")
    implementation("androidx.compose.animation:animation:1.4.0-alpha04")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.compose.animation:animation-graphics:1.3.3")
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("com.mikepenz:aboutlibraries-core:10.5.2")
    implementation("com.mikepenz:aboutlibraries-compose:10.5.2")
    "nonfreeImplementation"("com.android.billingclient:billing:5.1.0")
    "nonfreeImplementation"("com.android.billingclient:billing-ktx:5.1.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.mockk:mockk-agent-jvm:$mockkVersion")
    testImplementation("xmlpull:xmlpull:1.1.3.1")
    testImplementation("net.sf.kxml:kxml2:2.3.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("tools.fastlane:screengrab:2.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.3.3")
    debugImplementation("androidx.compose.ui:ui-tooling:1.3.3")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

tasks.withType<MergeResources>().configureEach {
    mustRunAfter("updateDrawables")
    mustRunAfter("importTranslations")
}