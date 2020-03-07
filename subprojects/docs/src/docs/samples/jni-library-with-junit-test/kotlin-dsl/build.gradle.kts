plugins {
	id("java")
	id("dev.nokee.jni-library")
	id("dev.nokee.cpp-language")
}

repositories {
	mavenCentral()
}

dependencies {
	testImplementation("junit:junit:4.12")
}
