// tag::configureViaGroup[]
plugins {
	id("java")
	id("dev.nokee.jni-library")
	id("dev.nokee.cpp-language")
}

group = "com.example.greeter"
// end::configureViaGroup[]

if (project.hasProperty("configureViaDsl")) {
// tag::configureViaDsl[]
	library {
		variants.configureEach {
			val osName = when {
				targetMachine.operatingSystemFamily.isWindows -> "windows"
				targetMachine.operatingSystemFamily.isLinux -> "linux"
				targetMachine.operatingSystemFamily.isMacOs -> "macos"
				else -> throw GradleException("Unknown operating system family")
			}
			val architectureName = when {
				targetMachine.architecture.is32Bit -> "x86"
				targetMachine.architecture.is64Bit -> "x86-64"
				else -> throw GradleException("Unknown architecture")
			}

			resourcePath.set("libs/${osName}-${architectureName}")
		}
	}
// end::configureViaDsl[]
}
