plugins {
	id("java")
	id("application")
}

application {
	mainClassName = "com.example.app.Main"
}

dependencies {
	implementation(project(":jni-library"))
}
