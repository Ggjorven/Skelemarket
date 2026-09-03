plugins {
    id("java")
    id("application")
    id("eclipse")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

group = "skelemarket"
version = "0.1-alpha"

repositories {
    mavenCentral()
}

sourceSets {
    main {
        java.setSrcDirs(listOf("src"))
    }
    test {
        java.setSrcDirs(listOf("tests"))
    }
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
	implementation("io.github.kusoroadeolu:ferrous:1.0.1")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("skelemarket.Main")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "skelemarket.Main"
    }
}

tasks.test {
    useJUnitPlatform()
}
