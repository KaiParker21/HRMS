plugins {
    alias(
        libs.plugins.android.application
    )
    alias(
        libs.plugins.kotlin.android
    )
    alias(
        libs.plugins.kotlin.compose
    )
    id("com.google.gms.google-services")
}

android {
    namespace =
        "com.skye.hrms"
    compileSdk =
        36

    defaultConfig {
        applicationId =
            "com.skye.hrms"
        minSdk =
            24
        targetSdk =
            36
        versionCode =
            1
        versionName =
            "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled =
                false
            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_11
        targetCompatibility =
            JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget =
            "11"
    }
    buildFeatures {
        compose =
            true
    }
}

dependencies {

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.2")

    //Coil
    implementation("io.coil-kt:coil-compose:2.4.0")

    // FireStore
    implementation("com.google.firebase:firebase-firestore-ktx")

    //LiveData
    implementation("androidx.compose.runtime:runtime-livedata:1.8.3")

    // Authentication
    implementation("com.google.firebase:firebase-auth")

    // Icons
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // Lottie
    implementation("com.airbnb.android:lottie-compose:6.6.7")

    // datastore
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // firebase ktx
    implementation("com.google.firebase:firebase-storage-ktx")

    // google fonts
    implementation("androidx.compose.ui:ui-text-google-fonts:1.8.3")

    //exo player
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")


    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Jetpack ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    implementation(
        libs.androidx.core.ktx
    )
    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )
    implementation(
        libs.androidx.activity.compose
    )
    implementation(
        platform(
            libs.androidx.compose.bom
        )
    )
    implementation(
        libs.androidx.ui
    )
    implementation(
        libs.androidx.ui.graphics
    )
    implementation(
        libs.androidx.ui.tooling.preview
    )
    implementation(
        libs.androidx.material3
    )
    testImplementation(
        libs.junit
    )
    androidTestImplementation(
        libs.androidx.junit
    )
    androidTestImplementation(
        libs.androidx.espresso.core
    )
    androidTestImplementation(
        platform(
            libs.androidx.compose.bom
        )
    )
    androidTestImplementation(
        libs.androidx.ui.test.junit4
    )
    debugImplementation(
        libs.androidx.ui.tooling
    )
    debugImplementation(
        libs.androidx.ui.test.manifest
    )
}