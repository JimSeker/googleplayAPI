plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    compileSdk = 37

    defaultConfig {
        applicationId = "edu.cs4730.googlelogindemo"
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
    buildFeatures {
        viewBinding = true
    }
    namespace = "edu.cs4730.googlelogindemo"
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
    implementation(libs.appcompat)
    implementation(libs.material)
    // Dependency forAuthenticate users with Sign in with Google and authorize access.
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    // Dependency for Google Sign-In button and authorize?
    implementation(libs.play.services.auth)
}