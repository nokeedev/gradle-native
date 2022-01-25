plugins {
	id("java")
	id("dev.nokee.jni-library")
	id("dev.nokee.objective-c-language")
}

library {
	targetMachines.set(listOf(machines.macOS))
	dependencies {
		nativeImplementation("dev.nokee.framework:Cocoa:latest.release")        		// <1>

		nativeImplementation("dev.nokee.framework:JavaNativeFoundation:latest.release") // <2>
		/* When using Xcode 12.1 and lower, use the following instead:
		nativeImplementation("dev.nokee.framework:JavaVM:latest.release")
		nativeImplementation("dev.nokee.framework:JavaVM:latest.release") {
			capabilities {
				requireCapability("JavaVM:JavaNativeFoundation:latest.release")
			}
		}
		*/
	}
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
