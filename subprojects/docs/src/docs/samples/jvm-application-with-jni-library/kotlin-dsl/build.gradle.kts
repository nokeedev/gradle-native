plugins {
	id("java")
	id("application")
}

import org.gradle.util.GradleVersion

application {
	if (GradleVersion.current() >= GradleVersion.version('6.7')) {
		mainClass.set("com.example.app.Main")
	} else {
		mainClassName = "com.example.app.Main"
	}
}

dependencies {
	implementation(project(":jni-library"))
}
