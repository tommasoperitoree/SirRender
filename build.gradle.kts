plugins {
	kotlin("jvm") version "2.3.0"
	id("org.jetbrains.dokka") version "2.1.0"
	application
}

group = "org.example"
version = "v0.1.0"

repositories {
	mavenCentral()
}

dependencies {
	testImplementation(kotlin("test"))                              // adds kotlin test helpers
	testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")   // aggregate: pulls api + engine together
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	// dokkaPlugin("org.jetbrains.dokka:mathjax-plugin:2.0.0")
}

kotlin {
	jvmToolchain(25)
}

application {
	mainClass.set("MainKt")
}

tasks.test {
	useJUnitPlatform()
}