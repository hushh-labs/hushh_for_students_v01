plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.project_gemini"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.project_gemini"
        minSdk = 24
        targetSdk = 34
        versionCode = 21
        versionName = "1.21"

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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding  = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-database:20.3.0")


    implementation ("com.github.sparrow007:carouselrecyclerview:1.2.6")
    implementation("com.github.bumptech.glide:glide:4.16.0")



    implementation("com.firebaseui:firebase-ui-database:8.0.2")
    implementation("com.github.sparrow007:carouselrecyclerview:1.2.6")
    implementation("com.google.android.gms:play-services-drive:17.0.0")






    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore:24.10.0")

    implementation("com.google.android.gms:play-services-mlkit-text-recognition-common:19.0.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")

    implementation("com.google.android.gms:play-services-auth:20.7.0")

    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    implementation("com.github.denzcoskun:ImageSlideshow:0.1.0")
    implementation("com.airbnb.android:lottie:6.2.0")

    implementation("com.github.ybq:Android-SpinKit:1.4.0")

    implementation("dev.shreyaspatil.EasyUpiPayment:EasyUpiPayment:3.0.3")





}