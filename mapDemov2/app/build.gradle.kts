plugins {
    alias(libs.plugins.android.application)
}

android {
    compileSdk = 37

    defaultConfig {
        applicationId = "edu.cs4730.mapdemov2"
        minSdk = 32
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.txt")
        }
    }
    buildFeatures {
        viewBinding = true
    }
    namespace = "edu.cs4730.mapdemov2"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.base)
    implementation(libs.constraintlayout)
    //bottomnavigation view
    implementation(libs.material)
    //navigation pieces.
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
}