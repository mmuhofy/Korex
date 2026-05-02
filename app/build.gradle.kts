plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace   = "com.muhofy.korex"
    compileSdk  = 35

    defaultConfig {
        applicationId   = "com.muhofy.korex"
        minSdk          = 26
        targetSdk       = 35
        versionCode     = 1
        versionName     = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-Xjvm-default=all"
    }

    buildFeatures {
        compose = true
    }

    // Disable Room schema verification — required when building on Android (RV2IDE)
    // Room's verifier needs a desktop SQLite native lib which is unavailable on aarch64 Android
    ksp {
        arg("room.verifyDatabase", "false")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        // Remove unused JNI libs shipped by terminal-view
        // libtermux.so    → TerminalSession (we use it — keep)
        // liblocal-socket → AmSocketServer  (not needed in Korex)
        jniLibs {
            excludes += "**/liblocal-socket.so"
        }
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.activity.compose)

    // Compose BOM — version managed centrally
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // Termux terminal engine
    implementation(libs.terminal.view)

    // Phosphor Icons
    implementation(libs.phosphor.icons)

    // Guava — provides ListenableFuture required by terminal-view and concurrent-futures
    implementation("com.google.guava:guava:33.0.0-android")

    // Required: androidx.concurrent.futures needed by profileinstaller
    implementation("androidx.concurrent:concurrent-futures:1.2.0")
}