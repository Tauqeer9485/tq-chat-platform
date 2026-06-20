import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.tq.distributed_chat_android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tq.distributed_chat_android"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties().apply {
            val propertiesFile = rootProject.file("local.properties")
            if (propertiesFile.exists()) {
                load(FileInputStream(propertiesFile))
            }
        }

        val apiUrl = properties.getProperty("API_URL") ?: "\"http://10.0.0.0:1234\""
        val wsUrl = properties.getProperty("CHATS_WS_URL") ?: "\"ws://10.0.0.0:2345\""
        val mediaUrl = properties.getProperty("MEDIA_URL") ?: "\"ws://10.0.0.0:3456\""

        buildConfigField("String", "API_URL", apiUrl)
        buildConfigField("String", "CHATS_WS_URL", wsUrl)
        buildConfigField("String", "MEDIA_URL", mediaUrl)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.activity)

    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.camerax.video)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.lifecycle.process)
    implementation(libs.room.common.jvm)
    implementation(libs.room.runtime)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    annotationProcessor(libs.room.compiler)
}