pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

if (!file(".git").exists()) {
    val errorText = """
        
        =====================[ ERROR ]=====================
         Please obtain the source code of Atlanta through Git clone.
        ===================================================
    """.trimIndent()
    error(errorText)
}

rootProject.name = "Atlanta"

include("atlanta-api", "atlanta-server")
