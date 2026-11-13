package com.clintmaples.openlevel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProjectContractTest {
    @Test
    fun manifestDoesNotRequestNetworkAccess() {
        val manifest = find("src/main/AndroidManifest.xml", "app/src/main/AndroidManifest.xml").readText()
        assertFalse(manifest.contains("INTERNET", ignoreCase = true))
        assertTrue(manifest.contains("android.permission.VIBRATE"))
        assertTrue(manifest.contains("android.hardware.sensor.accelerometer"))
        assertTrue(manifest.contains("android:required=\"false\""))
    }

    @Test
    fun releaseBuildIsPinnedAndDoesNotFallBackToTheDebugKey() {
        val gradle = find("build.gradle.kts", "app/build.gradle.kts").readText()
        assertTrue(gradle.contains("applicationId = \"com.clintmaples.openlevel\""))
        assertTrue(gradle.contains("versionName = \"0.1.0\""))
        assertTrue(gradle.contains("versionCode = 1"))
        assertTrue(gradle.contains("minSdk = 26"))
        assertTrue(gradle.contains("targetSdk = 35"))
        assertTrue(gradle.contains("CLINT_ANDROID_KEYSTORE"))
        assertTrue(gradle.contains("CLINT_ANDROID_STORE_PASSWORD"))
        assertTrue(gradle.contains("CLINT_ANDROID_KEY_ALIAS"))
        assertTrue(gradle.contains("CLINT_ANDROID_KEY_PASSWORD"))
        assertTrue(gradle.contains("Refusing to sign a release with the debug keystore."))
        assertFalse(gradle.contains("signingConfigs.debug"))
        assertFalse(gradle.contains("signingConfigs.getByName(\"debug\")"))
    }

    private fun find(vararg candidates: String): File {
        return candidates.map { File(it) }.firstOrNull { it.isFile }
            ?: error("Missing ${candidates.toList()} from ${File(".").absolutePath}")
    }
}
