plugins {
	kotlin("jvm") version "2.3.0"
	id("org.jetbrains.dokka") version "2.1.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	testImplementation(kotlin("test"))                              // adds kotlin test helpers
	testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")   // aggregate: pulls api + engine together
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	jvmToolchain(25)
}

tasks.test {
	useJUnitPlatform()
}