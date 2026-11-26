// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
// Compila fuera de OneDrive para que la sincronización no bloquee archivos.
allprojects {
    layout.buildDirectory.set(
        file("${System.getProperty("user.home")}/.gradle-builds/kaiju/${project.name}")
    )
}