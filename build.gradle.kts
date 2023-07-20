import io.papermc.paperweight.util.*

plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("io.papermc.paperweight.patcher") version "1.5.5"
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

repositories {
    mavenCentral()
    maven(paperMavenPublicUrl) {
        content { onlyForConfigurations(configurations.paperclip.name) }
    }
}

dependencies {
    remapper("net.fabricmc:tiny-remapper:0.8.6:fat")
    decompiler("net.minecraftforge:forgeflower:2.0.627.2")
    paperclip("io.papermc:paperclip:3.0.3")
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}

subprojects {
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
    }
}

paperweight {
    serverProject.set(project(":atlanta-server"))

    remapRepo.set(paperMavenPublicUrl)
    decompileRepo.set(paperMavenPublicUrl)

    useStandardUpstream("Kaiiju") {
        url.set(github("KaiijuMC", "Kaiiju"))
        ref.set(providers.gradleProperty("kaiijuRef"))

        withStandardPatcher {
            baseName("Kaiiju")

            apiPatchDir.set(layout.projectDirectory.dir("patches/api"))
            apiOutputDir.set(layout.projectDirectory.dir("atlanta-api"))

            serverPatchDir.set(layout.projectDirectory.dir("patches/server"))
            serverOutputDir.set(layout.projectDirectory.dir("atlanta-server"))
        }
    }
}

tasks.register("KaiijuRefLatest") {
    val tempDir = layout.cacheDir("KaiijuRefLatest");
    val file = "gradle.properties";

    doFirst {
        data class GithubCommit(
            val sha: String
        )

        val KaiijuLatestCommitJson = layout.cache.resolve("KaiijuLatestCommit.json");
        download.get().download("https://api.github.com/repos/KaiijuMC/Kaiiju/commits/ver/1.20.1", KaiijuLatestCommitJson);
        val KaiijuLatestCommit = gson.fromJson<paper.libs.com.google.gson.JsonObject>(KaiijuLatestCommitJson)["sha"].asString;

        copy {
            from(file)
            into(tempDir)
            filter { line: String ->
                line.replace("kaiijuRef=.*".toRegex(), "kaiijuRef=$KaiijuLatestCommit")
            }
        }
    }

    doLast {
        copy {
            from(tempDir.file("gradle.properties"))
            into(project.file(file).parent)
        }
    }
}