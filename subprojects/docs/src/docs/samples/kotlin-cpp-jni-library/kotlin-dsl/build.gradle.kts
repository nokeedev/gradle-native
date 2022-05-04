plugins {
	id("org.jetbrains.kotlin.jvm") version "1.6.21"
	id("dev.nokee.jni-library")
	id("dev.nokee.cpp-language")
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(platform(kotlin("bom")))
	implementation(kotlin("stdlib-jdk8"))

	testImplementation(kotlin("test"))
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }
