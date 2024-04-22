plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.matterdemosampleapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.matterdemosampleapp"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    // Connected Home
    implementation(libs.play.services.base)
    implementation(libs.play.services.home)

    // Matter Android Demo SDK
    implementation(libs.matter.android.demo.sdk)

    // Thread Network
//    implementation(libs.play.services.threadnetwork)
//    // Thread QR Code Scanning
//    implementation(libs.code.scanner)
//    // Thread QR Code Generation
//    implementation(libs.zxing)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.databinding.runtime)
    implementation(libs.legacy.support.v4)
    implementation(libs.preference)

    // Datastore
//    implementation(libs.datastore)
    implementation(libs.datastore.core)

    /*gson*/
    implementation(libs.gson)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.core)

    /*lottie animation*/
    implementation(libs.lottie)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.espresso.intents)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.rules)
    androidTestImplementation(libs.uiautomator)
}