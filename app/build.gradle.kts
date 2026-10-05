import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.paparazzi") version "2.0.0-alpha05.1"
}

val signing = Properties().apply {
    val f = file(System.getProperty("user.home") + "/.folio-signing/keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.purval.folio"
    compileSdk = 36

    packaging {
        jniLibs.useLegacyPackaging = true
        resources.excludes += "org/bouncycastle/pqc/**"  // post-quantum tables PDF decryption never uses
    }
    defaultConfig {
        // phones only: the Hindi translator's native code is ~17 MB per CPU type
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }
        applicationId = "com.purval.folio"
        minSdk = 26
        targetSdk = 36
        versionCode = 16
        versionName = "1.6.7"
    }

    signingConfigs {
        create("release") {
            if (signing.isNotEmpty()) {
                storeFile = file(signing.getProperty("storeFile"))
                storePassword = signing.getProperty("storePassword")
                keyAlias = signing.getProperty("keyAlias")
                keyPassword = signing.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
    buildFeatures { compose = true }
    androidResources { noCompress += "webp" }
}

dependencies {
    val bom = platform("androidx.compose:compose-bom:2025.10.01")
    implementation(bom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    implementation("com.google.mlkit:translate:17.0.3")
    testImplementation("junit:junit:4.13.2")
}
