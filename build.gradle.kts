/*
 * Sub-licenses:
 *         https://github.com/google/material-design-icons/blob/master/LICENSE
 *         https://github.com/Templarian/MaterialDesign/blob/master/LICENSE
 *         https://android.googlesource.com/platform/prebuilts/maven_repo/android/+/master/NOTICE.txt
 * This project:
 *         Copyright (C) 2019 Penn Mackintosh
 *         Licensed under https://www.gnu.org/licenses/gpl-3.0.en.html
 */

buildscript {
    val kotlinVersion = "1.4.30"
    extra["kotlinVersion"] = kotlinVersion
    repositories {
        google()
        mavenCentral()
        jcenter() // required by com.android.tools.build:gradle until 7.0.0-alpha01
        maven("https://jitpack.io")
        mavenLocal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.github.penn5:poeditor-android:0.1.1")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter() // https://github.com/fastlane/fastlane/issues/12651
        maven("https://jitpack.io")
        mavenLocal() // only for fastlane
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
