plugins {
    alias(libs.plugins.android.application)
}

android {
    compileSdk = 37

    defaultConfig {
        applicationId = "edu.cs4730.nearbyconstreamdemo"
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
    namespace = "edu.cs4730.nearbyconstreamdemo"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.play.services.nearby)
    implementation(libs.material)
    implementation(libs.activity)
}