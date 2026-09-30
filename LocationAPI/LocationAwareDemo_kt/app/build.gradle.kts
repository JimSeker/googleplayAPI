plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "edu.cs4730.locationawaredemo_kt"
    compileSdk = 37

    defaultConfig {
        applicationId = "edu.cs4730.locationawaredemo_kt"
        minSdk = 32
        targetSdk = 37
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation (libs.com.google.android.gms.play.services.location)
    implementation (libs.play.services.base)
    //Work Manager
    implementation (libs.androidx.work.runtime)
}