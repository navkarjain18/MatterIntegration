// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    /**
     * Use `apply false` in the top-level build.gradle file to add a Gradle plugin as a build
     * dependency but not apply it to the current (root) project. Don't use `apply false` in
     * sub-projects. For more information, see Applying external plugins with same version to
     * subprojects.
     */

    // Gradle Plugin
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false

    // Kotlin plugin
    alias(libs.plugins.kotlin.android) apply false

    // Hilt
    alias(libs.plugins.hilt) apply false
}