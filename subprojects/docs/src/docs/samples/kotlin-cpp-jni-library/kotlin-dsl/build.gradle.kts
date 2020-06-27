plugins {
	id("org.jetbrains.kotlin.jvm") version "1.3.72"
	id("dev.nokee.jni-library")
	id("dev.nokee.cpp-language")
}

repositories {
	jcenter()
}

dependencies {
	implementation(platform(kotlin("bom")))
	implementation(kotlin("stdlib-jdk8"))

	testImplementation(kotlin("test"))
	testImplementation(kotlin("test-junit"))
}
