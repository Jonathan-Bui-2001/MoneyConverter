import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

}

val localProperties = Properties()
file("../local.properties").inputStream().use { localProperties.load(it) }

val apiKey = localProperties.getProperty("apiKey") ?: "co_ loi_ da_xay_ra"

android {
    namespace = "com.example.moneyconverter"
    compileSdk = 33


    defaultConfig {
        applicationId = "com.example.moneyconverter"
        minSdk = 28
        targetSdk = 33
        versionCode = 2
        versionName = "0.2-beta"
        android.buildFeatures.buildConfig = true

        //sent api key to config
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner";

    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }


    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }



}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.code.gson:gson:2.10.1")



}