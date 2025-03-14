plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    alias(libs.plugins.indraSpotless)
}

if (rootProject.findProperty("snapshot")?.toString().equals("true")) {
    version = "$version-SNAPSHOT"
}

java {
    withSourcesJar()
    withJavadocJar()

    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.lettuce)
    implementation(libs.slf4jApi)
    implementation(libs.annotations)

    testImplementation(platform("org.junit:junit-bom:${rootProject.property("junit_version")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(libs.jedisMock)
    testRuntimeOnly(libs.sl4jSimple)
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

publishing {
    repositories {
        maven("https://maven.pkg.github.com/eupedroosouza/channels") {
            credentials {
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
        }
    }
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("LICENSE"))
    newLine(true)
}