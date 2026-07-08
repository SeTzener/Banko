import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.buildKonfig)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOption("bundleId", "com.banko.app")
        }
    }

    sourceSets {
        val commonMain by getting
        val androidMain by getting
        val commonTest by getting
        val androidUnitTest by getting

        iosMain.dependencies {
            implementation(libs.ktor.client.ios)
            implementation(libs.koin.core)
            implementation(libs.multiplatform.settings.no.arg)
        }

        commonMain.dependencies {
            implementation(libs.compose.material)
            implementation(compose.materialIconsExtended)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.material3AdaptiveNavigationSuite)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.decompose)
            implementation(libs.decompose.extensions)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.ktor.client)
            implementation(libs.ktor.client.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.room.runtime)
            implementation(libs.room.paging)
            implementation(libs.sqlite.bundle)

            api(libs.datastore)
            api(libs.datastore.preferences)
            api(libs.koin.core)
            implementation(libs.multiplatform.settings.no.arg)
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.decompose)
            implementation(libs.koin.android)
            implementation(libs.koin.android.compose)
            implementation(libs.ktor.client.android)
            implementation(libs.multiplatform.settings.no.arg)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidUnitTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-junit"))
            implementation(libs.mockk)
            implementation(libs.junit)
            implementation(libs.androidx.test.core)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.robolectric)
            implementation(libs.ktor.client.mock)
        }
    }
}

android {
    namespace = "com.banko.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
        compose = true
    }

    defaultConfig {
        applicationId = "com.banko.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}
dependencies {
    debugImplementation(compose.uiTooling)

    // Room target platform
    add("kspAndroid", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)

    // IDE help: explicitly declare Android test dependencies
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation(libs.mockk)
    testImplementation(libs.junit)
}

val env = Properties().apply {
    load(project.rootProject.file(".env").reader())
}

val nordigenId = env["NORDIGEN_ID"] as? String ?: throw IllegalArgumentException("API_KEY is missing in .env file")
val nordigenSecret = env["NORDIGEN_SECRET"] as? String ?: throw IllegalArgumentException("BASE_URL is missing in .env file")
val nordigenAccountId = env["NORDIGEN_ACCOUNT_ID"] as? String ?: throw IllegalArgumentException("NORDIGEN_ACCOUNT_ID is missing in .env file")

buildkonfig {
    packageName = "com.banko.config"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "NORDIGEN_ID", nordigenId)
        buildConfigField(FieldSpec.Type.STRING, "NORDIGEN_SECRET", nordigenSecret)
        buildConfigField(FieldSpec.Type.STRING, "NORDIGEN_ACCOUNT_ID", nordigenAccountId)
    }
}