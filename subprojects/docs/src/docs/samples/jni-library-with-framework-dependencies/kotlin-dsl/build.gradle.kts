plugins {
	id("java")
	id("dev.nokee.jni-library")
	id("dev.nokee.objective-c-language")
}

library {
	targetMachines.set(listOf(machines.macOS))
	dependencies {
		nativeImplementation("dev.nokee.framework:Cocoa:latest.release")        // <1>

		nativeImplementation("dev.nokee.framework:JavaVM:latest.release")
		nativeImplementation("dev.nokee.framework:JavaVM:latest.release") {
			capabilities {
				requireCapability("JavaVM:JavaNativeFoundation:latest.release") // <2>
			}
		}
	}
}

library.variants.configureEach {
	sharedLibrary {
		linkTask.configure {
			linkerArgs.add("-lobjc")
		}
	}
}
