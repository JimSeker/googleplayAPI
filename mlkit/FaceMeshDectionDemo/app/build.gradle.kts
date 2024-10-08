plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "edu.cs4730.facemeshdectiondemo"
    compileSdk = 35

    defaultConfig {
        applicationId = "edu.cs4730.facemeshdectiondemo"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.google.mlkit:face-mesh-detection:16.0.0-beta3")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
}