plugins {
	id("org.jetbrains.kotlin.jvm") version "1.6.10"
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
	testImplementation(kotlin("test-junit"))
}
