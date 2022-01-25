import dev.nokee.platform.nativebase.NativeBinary

plugins {
	id("dev.nokee.cpp-library")
}

description = "The C++ implementation, has no knowledge of the JVM."

library {
	// Note: it is possible to use a shared library.
	//     However you will need to write a loader aware of the multiple shared libraries.
	targetLinkages.set(listOf(linkages.static))

	binaries.configureEach(NativeBinary::class.java) {
		compileTasks.configureEach(AbstractNativeCompileTask::class.java) {
			isPositionIndependentCode = true
		}
	}
}
