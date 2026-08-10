plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.bottonnavigation_and_recyclerview_implement_homepage"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bottonnavigation_and_recyclerview_implement_homepage"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packaging {
        resources {
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/LICENSE.md"
        }
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.room:room-common-jvm:2.8.4")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.0")
    implementation("com.google.firebase:firebase-crashlytics:19.4.3")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.android.volley:volley:1.2.1")

    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    implementation("com.google.android.gms:play-services-ads:23.1.0")

    // WorkManager dependency
    implementation("androidx.work:work-runtime:2.9.1")
    // Fix for: cannot access ListenableFuture
    implementation("com.google.guava:guava:33.2.1-android")

    // Razorpay Checkout
    implementation("com.razorpay:checkout:1.6.38")

    // Lightweight Charts
    implementation("com.tradingview:lightweightcharts:3.8.0")

    //email dependency
    implementation("org.eclipse.angus:jakarta.mail:2.0.3")




}