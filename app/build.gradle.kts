plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.qrcodegenerator"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.qrcodegenerator"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // AndroidX dependencies
    implementation("commons-codec:commons-codec:1.15")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.cloudinary:cloudinary-android:2.2.0")
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
}