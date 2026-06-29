// build.gradle.kts

plugins {
	kotlin("jvm") version "2.3.0"
	id("org.jetbrains.dokka") version "2.2.0"
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
	jvmToolchain(25)
}

application {
	mainClass.set("cli.MainKt")
}

tasks.test {
	useJUnitPlatform()
}

// --- Dokka (V2 API) ---
dokka {
	moduleName.set("SirRender")
	
	dokkaSourceSets.main {
		// Landing page content
		includes.from("README-dokka.md")
		
		// Clickable source links to GitHub
		sourceLink {
			localDirectory.set(file("src/main/kotlin"))
			remoteUrl("https://github.com/tommasoperitoree/SirRender/blob/main/src/main/kotlin")
			remoteLineSuffix.set("#L")
		}
		
		// Hide the CLI package — it is an end-user tool, not part of the library API
		perPackageOption {
			matchingRegex.set("cli.*")
			suppress.set(true)
		}
		
		jdkVersion.set(25)
	}
}