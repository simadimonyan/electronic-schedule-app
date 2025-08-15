import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "2.0.0"
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.baselineprofile)
    id("ru.ok.tracer").version("1.0.1")
    id("com.google.devtools.ksp")
}

val properties = Properties().apply { load(File(rootProject.rootDir, "gradle.properties").inputStream()) }

val pushServerAccessToken = properties["pushserver.accessToken"] as String
val remoteConfigAppId = properties["remoteconfig.appId"] as String
val pushClientProjectId = properties["pushclient.projectId"] as String
val signingKeyPass = properties["signKeyPassword"] as String
val signingKeyPath = properties["signKeyPath"] as String
val tracerPluginToken = properties["tracer.pluginToken"] as String
val tracerAppToken = properties["tracer.appToken"] as String
val advertisementBannerId = properties["advertisement.bannerId"] as String

tracer {
    create("defaultConfig") {
        pluginToken = tracerPluginToken
        appToken = tracerAppToken

        uploadMapping = true
    }
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(signingKeyPath)
            keyAlias = "application"
            keyPassword = signingKeyPass
            storePassword = signingKeyPass
        }
    }
    namespace = "com.mycollege.schedule"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mycollege.schedule"
        minSdk = 29
        versionCode = 11
        versionName = "1.3.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("release")

            buildConfigField("String", "ACCESS_TOKEN", "\"$pushServerAccessToken\"")
            buildConfigField("String", "REMOTE_CONFIG_APP_ID", "\"$remoteConfigAppId\"")
            buildConfigField("String", "PUSH_CLIENT_PROJECT_ID", "\"$pushClientProjectId\"")
            buildConfigField("String", "ADVERTISEMENT_BANNER_ID", "\"$advertisementBannerId\"")
        }
        release {
            isMinifyEnabled = true //R8 compiler
            isShrinkResources = true //Shrinking
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")

            buildConfigField("String", "ACCESS_TOKEN", "\"$pushServerAccessToken\"")
            buildConfigField("String", "REMOTE_CONFIG_APP_ID", "\"$remoteConfigAppId\"")
            buildConfigField("String", "PUSH_CLIENT_PROJECT_ID", "\"$pushClientProjectId\"")
            buildConfigField("String", "ADVERTISEMENT_BANNER_ID", "\"$advertisementBannerId\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeCompiler {
        enableStrongSkippingMode = true
        reportsDestination = layout.buildDirectory.dir("compose_compiler")
        metricsDestination = layout.buildDirectory.dir("compose_compiler")
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources {
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

dependencies {
    implementation(libs.appupdate)

    implementation(libs.dagger.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.room.compiler)
    ksp(libs.androidx.room.room.compiler)

    implementation(libs.mobileads)

    implementation(libs.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation(libs.remoteconfig)

    implementation(libs.pushclient)

    implementation(platform(libs.tracer.platform))
    implementation(libs.tracer.crash.report)
    implementation(libs.tracer.crash.report.native)
    implementation(libs.tracer.heap.dumps)
    implementation(libs.tracer.disk.usage)
    implementation(libs.tracer.profiler.sampling)
    implementation(libs.tracer.profiler.systrace)

    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)

    implementation(libs.lottie.compose)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.animation)

    implementation(libs.androidx.hilt.work)
    implementation(libs.hilt.android)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.ui.tooling.preview.android)
    "baselineProfile"(project(":baselineprofile"))
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose.v120)

    implementation(libs.junit.jupiter)
    implementation(libs.androidx.runtime.livedata)

    testImplementation(libs.core.ktx)
    testImplementation(libs.truth)
    androidTestImplementation(libs.androidx.work.testing)
    testImplementation(libs.androidx.core.v127)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.gson)
    implementation(libs.jsoup)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.ui.tooling)
    implementation(libs.androidx.material3)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}