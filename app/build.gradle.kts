import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val signingPropertiesFile = rootProject.file("keystore.properties")
val signingProperties = Properties().apply {
    if (signingPropertiesFile.exists()) {
        signingPropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.darkstar.wallora"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.darkstar.wallora"
        minSdk = 26
        targetSdk = 37
        versionCode = 10
        versionName = "2.0"
    }

    val keystorePath = signingProperties.getProperty("storeFile")
        ?.takeIf { it.isNotBlank() }
        ?: System.getenv("ANDROID_KEYSTORE_PATH")
        ?: System.getenv("ANDROID_KEYSTORE_FILE")
    val keystorePassword = signingProperties.getProperty("storePassword")
        ?.takeIf { it.isNotBlank() }
        ?: System.getenv("ANDROID_KEYSTORE_PASSWORD")
    val keyAliasValue = signingProperties.getProperty("keyAlias")
        ?.takeIf { it.isNotBlank() }
        ?: System.getenv("ANDROID_KEY_ALIAS")
    val keyPasswordValue = signingProperties.getProperty("keyPassword")
        ?.takeIf { it.isNotBlank() }
        ?: System.getenv("ANDROID_KEY_PASSWORD")

    signingConfigs {
        create("release") {
            if (!keystorePath.isNullOrBlank()) storeFile = rootProject.file(keystorePath)
            if (!keystorePassword.isNullOrBlank()) storePassword = keystorePassword
            if (!keyAliasValue.isNullOrBlank()) keyAlias = keyAliasValue
            if (!keyPasswordValue.isNullOrBlank()) keyPassword = keyPasswordValue
        }
    }

    buildTypes {
        getByName("release") {
            if (!keystorePath.isNullOrBlank() &&
                !keystorePassword.isNullOrBlank() &&
                !keyAliasValue.isNullOrBlank() &&
                !keyPasswordValue.isNullOrBlank()
            ) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.08.00"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.work:work-runtime-ktx:2.11.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("io.coil-kt.coil3:coil-compose:3.6.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.6.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
