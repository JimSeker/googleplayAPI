plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    compileSdk = 37

    defaultConfig {
        applicationId = "edu.cs4730.fbdatabaseauthdemo"
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
    defaultConfig {
        // ...
        resConfigs("en") // And any other languages you support
    }
    buildFeatures {
        viewBinding = true
    }
    namespace = "edu.cs4730.fbdatabaseauthdemo"
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.material)

    //https://firebase.google.com/support/release-notes/android, the bom doesn't tell me when there is a update here, like others do.
    implementation(platform(libs.firebase.bom))
    // Google APIs, these are now in a simpler bom (bill of materials) library now, add which libraries without versions.
    implementation(libs.play.services.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.config)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics) //needed for remote-config.

    // Also add the dependencies for the Credential Manager libraries and specify their versions
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    //firebase ui, note as of 6.x  it must be androidx libs.
    //see this page for versioning, https://github.com/firebase/FirebaseUI-Android  not automatic, like above.
    // FirebaseUI for Firebase Realtime Database
    implementation(libs.firebaseui.database)
    // FirebaseUI for Firebase Auth
    implementation(libs.firebaseui.auth)
    // FirebaseUI for Cloud Storage
    implementation(libs.firebaseui.storage)
    // FirebaseUI for Cloud Firestore
    implementation(libs.firebaseui.firestore)
    implementation(libs.googleid)
}