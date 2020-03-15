plugins {
	id("java")
	id("dev.nokee.jni-library")
	id("dev.nokee.cpp-language")
	id("maven-publish")
	id("dev.nokee.samples.jni-library-publish")
}

group = "com.example.greeter"
version = "4.2"

library {
	targetMachines.set(listOf(
		machines.windows.x86_64,
		machines.macOS.x86_64,
		machines.linux.x86_64
	))
}


afterEvaluate {
	publishing {
		publications {
			create<MavenPublication>("jniLibrary") {
				from(components.getByName("jni"))
				artifactId = project.name
			}
			create<MavenPublication>("jniLibraryMacos") {
				from(components.getByName("jniMacos"))
				artifactId = "${project.name}-macos"
			}
			create<MavenPublication>("jniLibraryLinux") {
				from(components.getByName("jniLinux"))
				artifactId = "${project.name}-linux"
			}
			create<MavenPublication>("jniLibraryWindows") {
				from(components.getByName("jniWindows"))
				artifactId = "${project.name}-windows"
			}
		}
		repositories {
			maven {
				url = uri("${buildDir}/publishing-repository")
			}
		}
	}
}
