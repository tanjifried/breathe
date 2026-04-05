plugins {
  id("com.android.application")
  kotlin("android")
  id("org.jetbrains.kotlin.plugin.compose")
  id("com.google.devtools.ksp")
  id("com.google.dagger.hilt.android")
}

val breatheServerUrl = System.getenv("BREATHE_SERVER_URL") ?: "http://10.0.2.2:3000/"
val breatheVersionName = "0.3.2"

fun semverVersionCode(version: String): Int {
  val (major, minor, patch) = version.split('.').map(String::toInt)
  return (major * 10_000) + (minor * 100) + patch
}

android {
  namespace = "com.breathe"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.breathe"
    minSdk = 29
    targetSdk = 35
    versionCode = semverVersionCode(breatheVersionName)
    versionName = breatheVersionName
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    buildConfigField("String", "DEFAULT_SERVER_URL", "\"$breatheServerUrl\"")
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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {
  implementation(platform("androidx.compose:compose-bom:2024.12.01"))
  androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))

  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material:material-icons-extended")
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.activity:activity-compose:1.9.3")
  implementation("androidx.navigation:navigation-compose:2.8.5")
  implementation("com.google.android.material:material:1.12.0")

  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

  implementation("com.google.dagger:hilt-android:2.51")
  ksp("com.google.dagger:hilt-android-compiler:2.51")
  implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
  implementation("androidx.hilt:hilt-work:1.2.0")

  implementation("com.squareup.retrofit2:retrofit:2.11.0")
  implementation("com.squareup.retrofit2:converter-gson:2.11.0")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
  implementation("com.google.code.gson:gson:2.11.0")

  implementation("androidx.room:room-runtime:2.6.1")
  implementation("androidx.room:room-ktx:2.6.1")
  ksp("androidx.room:room-compiler:2.6.1")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

  implementation("androidx.security:security-crypto:1.1.0-alpha06")

  implementation("androidx.work:work-runtime-ktx:2.9.1")
  implementation("com.google.firebase:firebase-messaging-ktx:24.1.0")

  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")
}
