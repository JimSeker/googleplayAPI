import com.android.build.api.dsl.ViewBinding

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "edu.cs4730.selfiesegmentationdemo"
    compileSdk = 35

    defaultConfig {
        applicationId = "edu.cs4730.selfiesegmentationdemo"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {viewBinding =true}
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation ("com.google.mlkit:segmentation-selfie:16.0.0-beta6")
}
