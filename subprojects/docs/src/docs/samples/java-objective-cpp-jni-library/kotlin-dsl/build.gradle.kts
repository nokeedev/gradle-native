plugins {
	id("java")
	id("dev.nokee.jni-library")
	id("dev.nokee.objective-cpp-language")
}

library.variants.configureEach {
	sharedLibrary.linkTask.configure {
		linkerArgs.add("-lobjc")
	}
}
