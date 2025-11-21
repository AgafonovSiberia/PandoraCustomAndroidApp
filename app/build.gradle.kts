plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.pandorawear.mobile"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.pandorawear.customapi"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("release") {
            storeFile = file(project.findProperty("PANDORA_KEYSTORE_PATH") as String)
            storePassword = project.findProperty("PANDORA_KEY_PASSWORD") as String
            keyAlias = project.findProperty("PANDORA_KEY_ALIAS") as String
            keyPassword = project.findProperty("PANDORA_STORE_PASSWORD") as String
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.moshi.v1151)
    implementation(libs.moshi.kotlin.v1151)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.coroutines.android.v181)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.play.services.wearable)
    implementation(libs.play.services.wearable.v1820)

}