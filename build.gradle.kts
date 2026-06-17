plugins {
	kotlin("jvm") version "2.3.0"
	id("org.jetbrains.dokka") version "2.1.0"
	application
}

group = "sirrender"
version = "v0.3.0"

// --- SECURITY FIX ---
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
	testImplementation(kotlin("test"))
	testImplementation(libs.junit.jupiter)
	testRuntimeOnly(libs.junit.launcher)
	implementation(libs.clikt)
}

kotlin {
	jvmToolchain(21)   // ← 25 is non-LTS preview, use 21
}

application {
	mainClass.set("cli.MainKt")
}

tasks.test {
	useJUnitPlatform()
}

// --- Dokka ---
dokka {
	moduleName = "SirRender"
	dokkaSourceSets.main {
		includes.from("docs/module.md")   // ← landing page
		sourceLink {
			localDirectory.set(file("src/main/kotlin"))
			remoteUrl.set(uri("https://github.com/tommasoperitoree/SirRender/blob/main/src/main/kotlin"))
			remoteLineSuffix.set("#L")    // links to specific lines on GitHub
		}
	}
}