plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    id("signing")
    alias(libs.plugins.nexusPublishPlugin)
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
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])

            pom {
                name.set("channels")
                description.set("A simple API for creating Pub/Sub channels using lettuce")
                url.set("https://github.com/eupedroosouza/channels")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("http://www.opensource.org/licenses/mit-license.php")
                    }
                }

                developers {
                    developer {
                        id.set("eupedroosouza")
                        name.set("Pedro Souza")
                        email.set("66704494+eupedroosouza@users.noreply.github.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/eupedroosouza/channels.git")
                    developerConnection.set("scm:git:ssh://github.com/eupedroosouza/channels.git")
                    url.set("https://github.com/eupedroosouza/channels")
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(project.findProperty("sonatype.username")?.toString() ?: System.getenv("SONATYPE_USERNAME"))
            password.set(project.findProperty("sonatype.password")?.toString() ?: System.getenv("SONATYPE_PASSWORD"));
        }
    }
}

signing {
    val signedKey = project.findProperty("signed.key")?.toString() ?: System.getenv("GPG_SECRET_KEY")
    val signedPassword = project.findProperty("signed.password")?.toString() ?: System.getenv("GPG_PASSPHRASE")

    if (signedKey != null && signedPassword != null) {
        useInMemoryPgpKeys(signedKey, signedPassword)
    } else {
        useGpgCmd()
    }

    sign(publishing.publications[project.name])
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("LICENSE"))
    newLine(true)
}