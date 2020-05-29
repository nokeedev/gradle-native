plugins {
	id("dev.nokee.jni-library")
}

description = "The JNI library as the consumer would expect."

library {
	dependencies {
		api(project(":java-jni-greeter"))
		nativeImplementation(project(":cpp-jni-greeter"))
	}
}
