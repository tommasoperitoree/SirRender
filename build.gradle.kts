// build.gradle.kts

plugins {
	kotlin("jvm") version "2.3.0"
	id("org.jetbrains.dokka") version "2.2.0"
	application
}

group = "sirrender"
version = "v1.1.0"

// --- SECURITY FIX ---
// jackson-databind is a transitive dependency of Dokka 2.2.0.
// Pins both jackson artifacts to 2.18.8 to address:
//   #2 CVE: PolymorphicTypeValidator bypass via generic type parameters (High 8.1)
//   #3 CVE: array subtype allowlist bypass in BasicPolymorphicTypeValidator (High 8.1)
//   #4 CVE: InetSocketAddress deserialization triggers eager DNS resolution / SSRF (Moderate 5.3)
configurations.all {
	resolutionStrategy.eachDependency {
		if (requested.group == "com.fasterxml.jackson.core") {
			useVersion("2.18.8")
			because("pins all jackson-core artifacts to 2.18.8 to fix CVEs #2, #3, #4 introduced transitively via Dokka")
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