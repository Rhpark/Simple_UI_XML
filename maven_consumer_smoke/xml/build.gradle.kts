plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "kr.open.library.simple_ui.consumer.xml"
    compileSdk = 35

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(
        "${libs.versions.githubGroup.get()}:" +
            "${libs.versions.mavenArtifactIdXML.get()}:" +
            libs.versions.appVersion.get(),
    )
}

tasks.matching { it.name == "compileDebugKotlin" }.configureEach {
    dependsOn(
        ":simple_core:publishMavenPublicationToLocalConsumerRepository",
        ":simple_xml:publishMavenPublicationToLocalConsumerRepository",
    )
}
