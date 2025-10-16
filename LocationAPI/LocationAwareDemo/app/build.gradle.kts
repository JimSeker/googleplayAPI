plugins {
    alias(libs.plugins.androidApplication)
}

android {
    compileSdk = 36

    defaultConfig {
        applicationId = "edu.cs4730.locationawaredemo"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
        }
    }
    buildFeatures {
        viewBinding = true
    }
    namespace = "edu.cs4730.locationawaredemo"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation (libs.com.google.android.gms.play.services.location)
    implementation (libs.play.services.base)
    //Work Manager
    implementation (libs.androidx.work.runtime)
}


