plugins {
	id("java")
	id("dev.nokee.jni-library")
	id("dev.nokee.objective-c-language")
}

library.variants.configureEach {
	sharedLibrary {
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
			linkerArgs.addAll((this as LinkSharedLibrary).targetPlatform.map {
				when {
					it.operatingSystem.isFreeBSD -> listOf(File("/usr/local/lib"))
					else -> emptyList()
				}
			})
		}
	}
}
