plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.qihoo360.mobilesafe.report"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = false
    }
}

dependencies {
}
