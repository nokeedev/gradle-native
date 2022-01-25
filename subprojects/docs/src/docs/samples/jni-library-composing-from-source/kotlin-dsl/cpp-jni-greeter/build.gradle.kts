import org.gradle.internal.jvm.Jvm
import dev.nokee.platform.nativebase.NativeBinary

plugins {
	id("dev.nokee.cpp-library")
}

description = "The JNI shared library, also known as the native bindings."

library {
	// The native component of the JNI library needs to be a shared library.
	targetLinkages.set(listOf(linkages.shared))
	dependencies {
		implementation(project(":cpp-greeter"))
	}

	binaries.configureEach(NativeBinary::class.java) {
		compileTasks.configureEach {
			includes.from(compileTask.get().targetPlatform.map {
				listOf(File("${Jvm.current().javaHome.canonicalPath}/include")) + when {
					it.operatingSystem.isMacOsX -> listOf(File("${Jvm.current().javaHome.absolutePath}/include/darwin"))
					it.operatingSystem.isLinux -> listOf(File("${Jvm.current().javaHome.absolutePath}/include/linux"))
					it.operatingSystem.isWindows -> listOf(File("${Jvm.current().javaHome.absolutePath}/include/win32"))
					it.operatingSystem.isFreeBSD -> listOf(File("${Jvm.current().javaHome.absolutePath}/include/freebsd"))
					else -> emptyList()
				}
			})
			((AbstractNativeCompileTask) this).isPositionIndependentCode = true
		}
	}
}
