plugins {
	id("dev.nokee.objective-cpp-library")
	id("dev.nokee.xcode-ide")
}

import dev.nokee.platform.nativebase.SharedLibraryBinary

library.variants.configureEach {
	binaries.configureEach(SharedLibraryBinary::class.java) {
		// Some compiler on FreeBSD does not use local base
		compileTasks.configureEach({ it is AbstractNativeCompileTask }) {
			val compileTask = this as AbstractNativeCompileTask;
			compileTask.includes.from(compileTask.targetPlatform.map {
				when {
					it.operatingSystem.isFreeBSD -> listOf(File("/usr/local/include"))
					else -> emptyList()
				}
			})
		}
		linkTask.configure {
			linkerArgs.add("-lobjc")
		}
	}
}
