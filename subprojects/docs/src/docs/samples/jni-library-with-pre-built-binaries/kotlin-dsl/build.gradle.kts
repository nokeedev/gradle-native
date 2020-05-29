plugins {
	id("java")
	id("dev.nokee.jni-library")
	id("dev.nokee.cpp-language")
}

import java.util.concurrent.Callable

fun getLibraryFileNameFor(osFamily: dev.nokee.runtime.nativebase.OperatingSystemFamily): String {
	if (osFamily.isWindows) {
		return "${project.name}.dll"
	} else if (osFamily.isLinux) {
		return "lib${project.name}.so"
	} else if (osFamily.isMacOs) {
		return "lib${project.name}.dylib"
	}
	throw GradleException("Unknown operating system family '${osFamily}'.")
}

library {
	variants.configureEach {
		val prebuiltLibraryFile = file("pre-built-library/${getLibraryFileNameFor(targetMachine.operatingSystemFamily)}")
		if (prebuiltLibraryFile.exists()) { // <1>
			nativeRuntimeFiles.setFrom(prebuiltLibraryFile)
			nativeRuntimeFiles.from(CallableLogger({project.logger.warn("Using the pre-build library.")})) // <2>
		} else {
			nativeRuntimeFiles.from(CallableLogger({project.logger.warn("Building from the source.")})) // <2>
		}
	}
}

/**
 * A callable to log a message on the console only on the first call.
 */
class CallableLogger(logger:Runnable) : java.util.concurrent.Callable<List<File>> {
	private val logger:Runnable
	private var messageAlreadyLogged = false

	init {
		this.logger = logger
	}

	//	@Throws(Exception::class)
	override fun call(): List<File> {
		if (!messageAlreadyLogged) { // <3>
			logger.run()
			messageAlreadyLogged = true
		}
		return listOf()
	}
}
