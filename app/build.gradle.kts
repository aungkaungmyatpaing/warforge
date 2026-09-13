plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

/**
 * Reads an AdMob id from gradle.properties (or ~/.gradle/gradle.properties, or -P on the
 * command line) and falls back to Google's official TEST id when it is not set.
 */
fun adUnit(key: String, testId: String): String =
    (project.findProperty(key) as String?)?.takeIf { it.isNotBlank() } ?: testId

/**
 * Where the app looks for the published version manifest.
 *
 * A raw file in the project's own GitHub repository - not the REST API, which allows
 * sixty unauthenticated requests an hour per IP and would start failing as soon as two
 * players shared a carrier NAT. Raw files are served from a CDN and have no such limit.
 *
 * Set `updateManifestUrl` in ~/.gradle/gradle.properties. Left unset, the check is
 * simply skipped and the app never touches the network for it.
 */
val UPDATE_MANIFEST_URL: String = (project.findProperty("updateManifestUrl") as String?)
    ?.takeIf { it.startsWith("https://") } ?: ""

// Google's public test ids -- https://developers.google.com/admob/android/test-ads
val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
val TEST_BANNER = "ca-app-pub-3940256099942544/9214589741"
val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"

/**
 * Release signing, read from ~/.gradle/gradle.properties.
 *
 * The keystore and its passwords never go in the repository. Google Play identifies an
 * app by the key it was signed with for as long as it exists, so losing this file means
 * losing the ability to update the app at all - it is backed up like a password, not
 * committed like a source file.
 *
 * Create one with:
 *   keytool -genkeypair -v -keystore ~/keys/warforge.jks -alias warforge \
 *     -keyalg RSA -keysize 4096 -validity 10000
 *
 * Then in ~/.gradle/gradle.properties:
 *   warforgeStoreFile=/Users/you/keys/warforge.jks
 *   warforgeStorePassword=...
 *   warforgeKeyAlias=warforge
 *   warforgeKeyPassword=...
 *
 * With those unset the release build still runs and comes out unsigned, so a checkout
 * with no keys is never broken - it just cannot publish.
 */
fun signingProp(key: String): String? =
    (project.findProperty(key) as String?)?.takeIf { it.isNotBlank() }

val storeFilePath = signingProp("warforgeStoreFile")

/**
 * Shouts if a release build is about to ship with Google's test ad ids.
 *
 * The fallback to test ids is deliberate - a fresh checkout has to build - but it is
 * silent, and a release that keeps them earns nothing and shows every user a banner
 * reading "Test Ad". That is exactly the kind of mistake that is only noticed after the
 * rollout, so it is worth a line in the build log.
 */
fun warnAboutTestAds() {
    val missing = listOf("admobAppId", "admobBanner", "admobInterstitial", "admobRewarded")
        .filter { (project.findProperty(it) as String?).isNullOrBlank() }
    if (missing.isEmpty()) return
    logger.warn("")
    logger.warn("  ****************************************************************")
    logger.warn("  *  RELEASE BUILD IS USING ADMOB **TEST** IDS                    *")
    logger.warn("  *  Missing from ~/.gradle/gradle.properties:                    *")
    missing.forEach { logger.warn("  *    %-58s*".format(it)) }
    logger.warn("  *  This build will show \"Test Ad\" banners and earn nothing.     *")
    logger.warn("  ****************************************************************")
    logger.warn("")
}

gradle.taskGraph.whenReady {
    if (allTasks.any { it.name.contains("Release") }) warnAboutTestAds()
}

android {
    namespace = "com.naymyo.warforge"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.naymyo.warforge"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders["admobAppId"] = adUnit("admobAppId", TEST_APP_ID)
    }

    signingConfigs {
        if (storeFilePath != null && file(storeFilePath).exists()) {
            create("release") {
                storeFile = file(storeFilePath)
                storePassword = signingProp("warforgeStorePassword")
                keyAlias = signingProp("warforgeKeyAlias")
                keyPassword = signingProp("warforgeKeyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            // Store screenshots must not contain ads, and debug builds always serve
            // AdMob's test banner. `-PscreenshotMode=true` hides the slot entirely, so
            // captures come out at the full screen size and need no cropping.
            buildConfigField(
                "boolean", "HIDE_ADS",
                (project.findProperty("screenshotMode") as String?)?.toBoolean()?.toString()
                    ?: "false",
            )
            // Debug ALWAYS uses test ads. Clicking your own live ads gets you banned.
            buildConfigField("String", "AD_BANNER", "\"$TEST_BANNER\"")
            buildConfigField("String", "AD_INTERSTITIAL", "\"$TEST_INTERSTITIAL\"")
            buildConfigField("String", "AD_REWARDED", "\"$TEST_REWARDED\"")
            buildConfigField("String", "UPDATE_MANIFEST_URL", "\"$UPDATE_MANIFEST_URL\"")
        }
        release {
            signingConfig = signingConfigs.findByName("release")
            // Never in a shipping build, whatever is passed on the command line.
            buildConfigField("boolean", "HIDE_ADS", "false")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            buildConfigField("String", "AD_BANNER", "\"${adUnit("admobBanner", TEST_BANNER)}\"")
            buildConfigField(
                "String", "AD_INTERSTITIAL", "\"${adUnit("admobInterstitial", TEST_INTERSTITIAL)}\""
            )
            buildConfigField("String", "AD_REWARDED", "\"${adUnit("admobRewarded", TEST_REWARDED)}\"")
            buildConfigField("String", "UPDATE_MANIFEST_URL", "\"$UPDATE_MANIFEST_URL\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    testOptions {
        // Geometry code touches android.graphics.Color; the JVM stub returns 0
        // rather than throwing, which is all the catalog/rule tests need.
        unitTests.isReturnDefaultValues = true
    }
}

// The offline preview renderer reads the Blender exports straight off disk, so a
// re-exported model has to count as a change to the tests. Without this the task stays
// up to date and silently re-publishes the previous render.
tasks.withType<Test>().configureEach {
    inputs.dir(layout.projectDirectory.dir("src/main/assets/models"))
        .withPropertyName("models")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.process)

    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)

    testImplementation(libs.junit)
    // The glTF loader parses with org.json, which the mockable android.jar stubs out to
    // return nothing. A real implementation on the test classpath lets it be tested.
    testImplementation(libs.json)
}
