plugins {
	kotlin("jvm") version "2.3.0"
	id("org.jetbrains.dokka") version "2.1.0"
	application
}

group = "org.example"
version = "v0.1.0"

// --- SECURITY FIX ---
// This block intercepts all dependencies across all configurations (including Dokka)
configurations.all {
	resolutionStrategy.eachDependency {
		if (requested.group == "com.fasterxml.jackson.core" && requested.name == "jackson-core") {
			useVersion("2.18.6")
			because("fixes a DoS vulnerability in the async parser (GHSA-72hv-8253-57qq)")
		}
	}
}

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

application {
	mainClass.set("MainKt")
}

tasks.test {
	useJUnitPlatform()
}