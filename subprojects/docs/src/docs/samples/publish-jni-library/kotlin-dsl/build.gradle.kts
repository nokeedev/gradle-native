plugins {
	id("java")
	id("dev.nokee.jni-library")
	id("dev.nokee.cpp-language")
	id("maven-publish")
}

group = "com.example.greeter"
version = "4.2"

publishing {
	publications {
		create<MavenPublication>("jniLibrary") {
			from(components.getByName("java"))
		}
	}
	repositories {
		maven {
			url = uri("${buildDir}/publishing-repository")
		}
	}
}
