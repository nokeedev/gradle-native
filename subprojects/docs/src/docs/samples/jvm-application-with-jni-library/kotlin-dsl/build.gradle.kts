plugins {
	id("java")
	id("application")
}

import org.gradle.util.GradleVersion
import org.gradle.api.plugins.JavaApplication

// Create a shim method to support newer Gradle version
fun JavaApplication.setMainClass(className: String) {
	if (GradleVersion.current() >= GradleVersion.version("6.7")) {
		val getMainClass = javaClass.getMethod("getMainClass")
		val mainClass = getMainClass.invoke(this) as Property<String>
		mainClass.set("com.example.app.Main")
	} else {
		mainClassName = "com.example.app.Main"
	}
}

application {
	setMainClass("com.example.app.Main")
}

dependencies {
	implementation(project(":jni-library"))
}
