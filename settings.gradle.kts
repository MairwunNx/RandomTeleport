pluginManagement {
    repositories {
        jcenter().apply { gradlePluginPortal() }
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
    }
    plugins {
        id("fabric-loom") version extra.properties["loom_version"].toString()
        kotlin("jvm") version extra.properties["kotlin_version"].toString()
    }
}
