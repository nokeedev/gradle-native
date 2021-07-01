plugins {
	id("java-library")
	id("groovy-base")
}

import dev.gradleplugins.GradleRuntimeCompatibility.*

java {
	targetCompatibility = minimumJavaVersionFor("5.1")	// <1>
	sourceCompatibility = minimumJavaVersionFor("5.1")	// <1>
}

repositories {
	gradlePluginDevelopment()							// <2>
	mavenCentral()										// <3>
}

dependencies {
	compileOnly(gradleApi("5.1"))						// <4>

	testImplementation(gradleApi("5.1"))				// <5>
	testImplementation(platform("org.spockframework:spock-bom:2.0-groovy-2.5"))
	testImplementation("org.spockframework:spock-core")
}

tasks.register("gradleCompatibility") {
	val gradleVersion = "5.1"
	doLast {
		println("=== Gradle ${gradleVersion} Compatibility Information ===")
		println("Minimum Java version: ${minimumJavaVersionFor(gradleVersion)}")
		println("Groovy version: ${groovyVersionOf(gradleVersion)}")
		println("Kotlin version: ${kotlinVersionOf(gradleVersion).orElse("N/A")}")
	}
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }
