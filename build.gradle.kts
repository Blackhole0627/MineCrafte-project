// Build config para quem preferir usar Gradle no projeto real.
// A demo tambem pode ser compilada com o script build.ps1 (javac puro),
// util enquanto o Gradle nao esta instalado na maquina.
plugins {
    java
}

group = "com.seasonrpg"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

tasks.processResources {
    filteringCharset = "UTF-8"
}

tasks.jar {
    archiveFileName.set("SeasonRPGDemo-${version}.jar")
}
