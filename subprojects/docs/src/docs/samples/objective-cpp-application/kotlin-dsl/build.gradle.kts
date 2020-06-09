plugins {
	id("dev.nokee.objective-cpp-application")
	id("dev.nokee.xcode-ide")
}

import dev.nokee.platform.nativebase.ExecutableBinary

application.variants.configureEach {
	binaries.configureEach(ExecutableBinary::class.java) {
		linkTask.configure {
			linkerArgs.add("-lobjc")
		}
	}
}
