plugins {
    alias(libs.plugins.android.application)
}

android {
    compileSdk = 37

    defaultConfig {
        applicationId = "edu.cs4730.admobdemo"
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
    namespace = "edu.cs4730.admobdemo"
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    //https://developers.google.com/android/guides/releases
    implementation(libs.play.services.ads)
    //implementation ("com.google.firebase:firebase-ads:25.5.0")  //likely the same library as gp ads at this point.
    //implementation("com.google.android.ads.consent:consent-library:1.0.8")
    implementation(libs.user.messaging.platform)
    implementation(libs.work.runtime)
}