plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "edu.cs4730.posedemo"
    compileSdk = 37

    defaultConfig {
        applicationId = "edu.cs4730.posedemo"
        minSdk = 32
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    // CameraX core library using camera2 implementation
    implementation(libs.camera.camera2)
    // CameraX Lifecycle Library
    implementation(libs.camera.lifecycle)
    // CameraX View class
    implementation(libs.camera.view)
    // If you want to additionally use the CameraX Extensions library, not used in this example.
    implementation(libs.camera.extensions)
    // If you want to use the base sdk  https://developers.google.com/ml-kit/release-notes
    implementation(libs.mlkit.pose.detection)
    // If you want to use the accurate sdk
    implementation(libs.mlkit.pose.detection.accurate)
    //implementation 'com.google.guava:guava:33.1-android'
}