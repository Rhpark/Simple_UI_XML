pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://jitpack.io")
    }
}

val localConsumerPublication = providers
    .gradleProperty("localConsumerPublication")
    .map { it.equals("true", ignoreCase = true) }
    .getOrElse(false)

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        if (localConsumerPublication) {
            exclusiveContent {
                forRepository {
                    maven {
                        name = "localConsumer"
                        url = uri(rootDir.resolve("build/consumer-maven"))
                    }
                }
                filter {
                    includeGroup("io.github.rhpark")
                }
            }
        }
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "SimpleUI_XML"
include(":app")
include(":simple_core")
include(":simple_xml")
include(":simple_system_manager")
include(":simple_compose")
if (localConsumerPublication) {
    include(":maven_consumer_smoke")
    include(":maven_consumer_smoke:core")
    include(":maven_consumer_smoke:xml")
    include(":maven_consumer_smoke:compose")
    include(":maven_consumer_smoke:system_manager")
}
