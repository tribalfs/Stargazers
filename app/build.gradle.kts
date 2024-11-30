import java.util.Properties

plugins {
    id ("com.android.application")
    id ("kotlin-android")
    id ("kotlin-parcelize")
    id ("com.google.devtools.ksp")
}

val githubProperties = Properties().apply {
    rootProject.file("github.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

android {
    compileSdk = 35
    namespace = "com.tribalfs.stargazers"

    defaultConfig {
        applicationId = "com.tribalfs.stargazers"

        minSdk = 23
        targetSdk = 35

        versionCode = 5
        versionName = "0.0.5"

        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    //sesl6 modules
    implementation("sesl.androidx.core:core:1.15.0+1.0.11-sesl6+rev0")
    implementation("sesl.androidx.core:core-ktx:1.15.0+1.0.0-sesl6+rev0")
    implementation("sesl.androidx.fragment:fragment:1.8.4+1.0.0-sesl6+rev1")
    implementation("sesl.androidx.appcompat:appcompat:1.7.0+1.0.34-sesl6+rev6")
    implementation("sesl.androidx.picker:picker-basic:1.0.17+1.0.17-sesl6+rev2")
    implementation("sesl.androidx.picker:picker-color:1.0.6+1.0.6-sesl6+rev3")
    implementation("sesl.androidx.preference:preference:1.2.1+1.0.4-sesl6+rev3")
    implementation("sesl.androidx.recyclerview:recyclerview:1.4.0-rc01+1.0.21-sesl6+rev0")
    implementation("sesl.androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01+1.0.0-sesl6+rev0")
    implementation("sesl.androidx.apppickerview:apppickerview:1.0.1+1.0.1-sesl6+rev3")
    implementation("sesl.androidx.indexscroll:indexscroll:1.0.3+1.0.3-sesl6+rev3")
    implementation("sesl.androidx.viewpager2:viewpager2:1.1.0+1.0.0-sesl6+rev0")
    implementation("sesl.com.google.android.material:material:1.12.0+1.0.23-sesl6+rev2")

    //design lib
    implementation("io.github.tribalfs:oneui-design:0.2.8+oneui6")

    //These are still OneUI4/5 icons.
    //Hopefully someone will update this.
    implementation("io.github.oneuiproject:icons:1.1.0")

    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    implementation("com.airbnb.android:lottie:6.5.2")


    implementation("androidx.datastore:datastore-preferences-android:1.1.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.squareup.moshi:moshi:1.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.navigation:navigation-fragment-ktx:2.8.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.4")
}

configurations.implementation {
    //Exclude official android jetpack modules
    exclude ("androidx.core",  "core")
    exclude ("androidx.core",  "core-ktx")
    exclude ("androidx.customview",  "customview")
    exclude ("androidx.coordinatorlayout",  "coordinatorlayout")
    exclude ("androidx.drawerlayout",  "drawerlayout")
    exclude ("androidx.viewpager2",  "viewpager2")
    exclude ("androidx.viewpager",  "viewpager")
    exclude ("androidx.appcompat", "appcompat")
    exclude ("androidx.fragment", "fragment")
    exclude ("androidx.preference",  "preference")
    exclude ("androidx.recyclerview", "recyclerview")
    exclude ("androidx.slidingpanelayout",  "slidingpanelayout")
    exclude ("androidx.swiperefreshlayout",  "swiperefreshlayout")

    //Exclude official material components lib
    exclude ("com.google.android.material",  "material")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

