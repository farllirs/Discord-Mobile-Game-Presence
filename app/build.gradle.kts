plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose") }
android {
 namespace = "dev.farllirs.presence"; compileSdk = 35
 defaultConfig { applicationId = "dev.farllirs.presence"; minSdk = 26; targetSdk = 35; versionCode = 2; versionName = "0.2.0-alpha.1"; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
 val releaseStore = providers.environmentVariable("RELEASE_STORE_FILE").orNull?.takeIf { it.isNotBlank() }
 signingConfigs { if (releaseStore != null) create("release") { storeFile = file(releaseStore); storePassword = providers.environmentVariable("RELEASE_STORE_PASSWORD").orNull; keyAlias = providers.environmentVariable("RELEASE_KEY_ALIAS").orNull; keyPassword = providers.environmentVariable("RELEASE_KEY_PASSWORD").orNull } }
 buildTypes { getByName("release") { isMinifyEnabled = false; if (releaseStore != null) signingConfig = signingConfigs.getByName("release") } }
 buildFeatures { compose = true }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
dependencies {
 val bom = platform("androidx.compose:compose-bom:2025.03.00")
 implementation(bom); androidTestImplementation(bom)
 implementation("androidx.core:core-ktx:1.15.0"); implementation("androidx.activity:activity-compose:1.10.1"); implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7"); implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
 implementation("androidx.compose.ui:ui"); implementation("androidx.compose.ui:ui-tooling-preview"); implementation("androidx.compose.material3:material3"); implementation("androidx.compose.material:material-icons-extended"); implementation("androidx.datastore:datastore-preferences:1.1.4")
 testImplementation("junit:junit:4.13.2"); androidTestImplementation("androidx.test.ext:junit:1.2.1"); androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1"); androidTestImplementation("androidx.compose.ui:ui-test-junit4"); debugImplementation("androidx.compose.ui:ui-tooling"); debugImplementation("androidx.compose.ui:ui-test-manifest")
}
