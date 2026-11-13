plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val requiredSigningEnv = listOf(
    "CLINT_ANDROID_KEYSTORE",
    "CLINT_ANDROID_STORE_PASSWORD",
    "CLINT_ANDROID_KEY_ALIAS",
    "CLINT_ANDROID_KEY_PASSWORD",
)

val keystorePath = System.getenv("CLINT_ANDROID_KEYSTORE")
val keystoreStorePassword = System.getenv("CLINT_ANDROID_STORE_PASSWORD")
val keystoreKeyAlias = System.getenv("CLINT_ANDROID_KEY_ALIAS")
val keystoreKeyPassword = System.getenv("CLINT_ANDROID_KEY_PASSWORD")
val releaseSigningReady = requiredSigningEnv.all { !System.getenv(it).isNullOrBlank() } &&
    !keystorePath.isNullOrBlank() &&
    file(keystorePath).isFile

android {
    namespace = "com.clintmaples.openlevel"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.clintmaples.openlevel"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        create("release") {
            if (releaseSigningReady) {
                storeFile = file(keystorePath!!)
                storePassword = keystoreStorePassword
                keyAlias = keystoreKeyAlias
                keyPassword = keystoreKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (releaseSigningReady) {
                signingConfig = signingConfigs.getByName("release")
            }
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
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
    }
}

tasks.register("ensureReleaseSigning") {
    doLast {
        val missing = requiredSigningEnv.filter { System.getenv(it).isNullOrBlank() }
        if (missing.isNotEmpty()) {
            throw GradleException(
                "Cannot build a release: missing signing environment variable(s): " +
                    missing.joinToString(", ") +
                    ". Refusing to sign a release with the debug keystore.",
            )
        }
        val path = System.getenv("CLINT_ANDROID_KEYSTORE")
        if (path.isNullOrBlank() || !file(path).isFile) {
            throw GradleException(
                "Cannot build a release: CLINT_ANDROID_KEYSTORE does not point to a keystore file. " +
                    "Refusing to sign a release with the debug keystore.",
            )
        }
    }
}

tasks.configureEach {
    if (
        name == "assembleRelease" ||
        name == "bundleRelease" ||
        name == "packageRelease" ||
        name == "signReleaseBundle" ||
        name == "packageReleaseBundle"
    ) {
        dependsOn("ensureReleaseSigning")
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
}
