import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// 从项目根目录的 keystore.properties 读签名配置（该文件已 gitignore，不会提交）
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.example.music"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.music"
        minSdk = 24
        targetSdk = 37
        versionCode = 10
        versionName = "1.5.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 高德 SDK 的 native 库支持以下 ABI
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
    }

    signingConfigs {
        // 只有本机存在 keystore.properties 时才创建，保证别人 clone 后也能正常构建
        if (keystoreProps.getProperty("storeFile") != null) {
            create("release") {
                // keystore 是 PKCS12 格式，必须显式声明，
                // 否则 AGP 按 JKS 解析会报 "Given final block not properly padded"
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storeType = keystoreProps.getProperty("storeType") ?: "PKCS12"
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // 有签名配置就用正式签名，避免打出未签名包
            signingConfigs.findByName("release")?.let { signingConfig = it }
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

    buildFeatures {
        compose = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    // ===== 基础 =====
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ===== Compose 额外 =====
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // ===== Media3 =====
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.datasource)
    implementation(libs.androidx.media3.datasource.okhttp)

    // ===== 网络 / 协程 / 图片 =====
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil.compose)

    // ===== Room =====
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ===== Palette =====
    implementation(libs.androidx.palette)

    // ===== 高德地图 =====
    implementation("com.amap.api:3dmap-location-search:11.2.100_loc11.2.100_sea9.8.1")

    // ===== 测试 =====
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}