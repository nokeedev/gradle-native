plugins {
	id("cpp-library")
}

description = "The C++ implementation, has no knowledge of the JVM."

library {
	// Note: it is possible to use a shared library.
	//     However you will need to write a loader aware of the multiple shared libraries.
	linkage.set(listOf(Linkage.STATIC))

	binaries.configureEach {
		compileTask.get().setPositionIndependentCode(true)
	}
}
