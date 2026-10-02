plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    compileSdk = 37

    defaultConfig {
        applicationId = "edu.cs4730.firebasefirestoredemo"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    namespace = "edu.cs4730.firebasefirestoredemo"
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.activity)
    //https://firebase.google.com/support/release-notes/android, the bom doesn't tell me when there is a update here, like others do.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.analytics)
    // FirebaseUI for Firebase Auth
    //see this page for versioning, https://github.com/firebase/FirebaseUI-Android  not automatic, like above.
    implementation(libs.firebaseui.auth)
    // FirebaseUI for Cloud Firestore
    implementation(libs.firebaseui.firestore)
}