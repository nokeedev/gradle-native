plugins {
	id("java")
	id("dev.nokee.jni-library")
}

import java.util.concurrent.Callable
import dev.nokee.runtime.nativebase.OperatingSystemFamily

fun getLibraryFilePathFor(osFamily: OperatingSystemFamily): String {
	if (osFamily.isWindows) {
		return "pre-built-libraries/windows/jni-greeter.dll"
	} else if (osFamily.isLinux) {
		return "pre-built-libraries/linux/libjni-greeter.so"
	} else if (osFamily.isMacOs) {
		return "pre-built-libraries/macos/libjni-greeter.dylib"
	}
	throw GradleException("Unknown operating system family '${osFamily}'.")
}

library {
	targetMachines.set(listOf(machines.windows.x86_64, machines.linux.x86_64, machines.macOS.x86_64))
	variants.configureEach {
		val prebuiltLibraryFile = file(getLibraryFilePathFor(targetMachine.operatingSystemFamily))
		nativeRuntimeFiles.setFrom(prebuiltLibraryFile)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	testImplementation("junit:junit:4.12")
}
