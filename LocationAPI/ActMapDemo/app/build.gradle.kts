plugins {
    alias(libs.plugins.android.application)
}

android {
    compileSdk = 37

    defaultConfig {
        applicationId = "edu.cs4730.actmapdemo"
        minSdk = 32
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        buildConfig = true
    }
    namespace = "edu.cs4730.actmapdemo"
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.viewpager2)
    implementation(libs.activity)
    // Dependency for Google location aware stuff.
    //see https://developers.google.com/android/guides/setup for the whole list.
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.base)
    //viewmodel
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
}