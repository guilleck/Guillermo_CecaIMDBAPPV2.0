plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "es.riberadeltajo.ceca_guillermoimdbapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "es.riberadeltajo.ceca_guillermoimdbapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.auth)
    implementation(libs.activity)
    implementation(libs.security.crypto)
    implementation(libs.places)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.facebook.android:facebook-android-sdk:16.1.2")
    implementation ("com.facebook.android:facebook-login:16.1.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation (platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation ("com.google.android.gms:play-services-auth:20.5.0")
    implementation ("com.google.firebase:firebase-auth:23.1.0")
    implementation ("com.github.bumptech.glide:glide:4.14.2")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.14.2")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.code.gson:gson:2.8.8")
    implementation ("com.google.firebase:firebase-firestore")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.libraries.places:places:2.6.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.hbb20:ccp:2.5.1")
    implementation("com.googlecode.libphonenumber:libphonenumber:8.12.50")
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation ("androidx.work:work-runtime:2.7.1")














}