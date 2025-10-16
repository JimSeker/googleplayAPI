plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "edu.cs4730.fusedorienationdemo"
    compileSdk = 36

    defaultConfig {
        applicationId = "edu.cs4730.fusedorienationdemo"
        minSdk = 31
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    implementation (libs.play.services.location)
    implementation (libs.play.services.base)
}