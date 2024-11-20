import java.util.Properties

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.7.2" apply false
    id("com.android.library") version "8.7.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
}

/**
 * Note: To configure GitHub credentials, you have to generate an access token with at least
 * `read:packages` scope at https://github.com/settings/tokens/new and then
 * add it to any of the following:
 * <ul>
 *      <li>Add `githubUsername` and `githubAccessToken` to Global Gradle Properties</li>
 *      <li>Set `GITHUB_USERNAME` and `GITHUB_ACCESS_TOKEN` in your environment variables</li>
 *      <li>Create a `github.properties` file in your project folder with the following content:</li>
 * </ul>
 *
 * <pre>
 *   githubUsername=&lt;YOUR_GITHUB_USERNAME&gt;
 *   githubAccessToken=&lt;YOUR_GITHUB_ACCESS_TOKEN&gt;
 * </pre>
 */
val githubProperties = Properties().apply {
    rootProject.file("github.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

val githubUsername: String = rootProject.findProperty("githubUsername") as String? // Global Gradle Properties
    ?: githubProperties.getProperty("githubUsername") // github.properties file
    ?: System.getenv("GITHUB_USERNAME") // Environment Variables
    ?: error("GitHub username not found")

val githubAccessToken: String = rootProject.findProperty("githubAccessToken") as String? // Global Gradle Properties
    ?: githubProperties.getProperty("githubAccessToken") // github.properties file
    ?: System.getenv("GITHUB_ACCESS_TOKEN") // Environment Variables
    ?: error("GitHub Access Token not found")


allprojects {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven { url =  uri("https://jitpack.io") }
        maven {
            url = uri("https://maven.pkg.github.com/tribalfs/sesl-androidx")
            credentials {
                username = githubUsername
                password = githubAccessToken
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/tribalfs/sesl-material-components-android")
            credentials {
                username = githubUsername
                password = githubAccessToken
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/tribalfs/oneui-design")
            credentials {
                username = githubUsername
                password = githubAccessToken
            }
        }
    }


}

subprojects {
    afterEvaluate {
        if (plugins.hasPlugin("com.android.application")) {
            val oneUiDesignVersion = project.configurations.getByName("implementation").dependencies.find {
                it.group == "io.github.tribalfs" && it.name == "oneui-design" }?.version

            extensions.configure<com.android.build.gradle.BaseExtension> {
                defaultConfig {
                    buildConfigField("String", "ONEUI_DESIGN_VERSION", "\"$oneUiDesignVersion\"")
                }
                buildTypes.all{
                    buildConfigField("String", "ONEUI_DESIGN_VERSION", "\"$oneUiDesignVersion\"")
                }
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)

}
