plugins {
	id("dev.nokee.objective-c-application")
	id("dev.nokee.xcode-ide")
}

import dev.nokee.platform.nativebase.ExecutableBinary

application.variants.configureEach {
	binaries.configureEach(ExecutableBinary::class.java) {
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
			linkerArgs.addAll((this as LinkExecutable).targetPlatform.map {
				when {
					it.operatingSystem.isFreeBSD -> listOf("-L/usr/local/lib")
					else -> emptyList()
				}
			})
		}
	}
}
