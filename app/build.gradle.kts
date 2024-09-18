plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.mikepenz.aboutlibraries.plugin")
    id("kotlin-parcelize")
}

android {
    namespace = "com.king250.kirafan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.king250.kirafan"
        minSdk = 26
        targetSdk = 35
        versionCode = 40000
        versionName = "4.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
            isUniversalApk = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"))))
    implementation(platform("androidx.compose:compose-bom:2024.09.01"))
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.alibaba.fastjson2:fastjson2-kotlin:2.0.52")
    implementation("com.mikepenz:aboutlibraries-core:11.1.4")
    implementation("com.mikepenz:aboutlibraries-compose-m3:11.1.4")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("androidx.test:runner:1.6.1")
    implementation("androidx.test.espresso:espresso-core:3.6.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.01"))
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}