plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    compileSdk = 37

    defaultConfig {
        applicationId = "edu.cs4730.firebasemessagedemo"
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
    buildFeatures {
        viewBinding = true
    }
    namespace = "edu.cs4730.firebasemessagedemo"
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    //https://firebase.google.com/support/release-notes/android, the bom doesn't tell me when there is a update here, like others do.
    implementation(platform(libs.firebase.bom))
    //firebase, docs say not include core anymore, instead analytics.  don't think I need analytics, but including it anyway.
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)

    //replaces the jobservice with a worker instead,  jobservice is depreciated.
    implementation(libs.work.runtime)

    //https://developer.android.com/training/volley/
    implementation(libs.volley)
}