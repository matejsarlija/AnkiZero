// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.services) apply false // Added
    //id("com.google.gms.google-services") version "4.4.2" apply false
    alias(libs.plugins.firebase.crashlytics) apply false // Added
}
