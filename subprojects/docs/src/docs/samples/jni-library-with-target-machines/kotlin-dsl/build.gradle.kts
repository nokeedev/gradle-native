plugins {
	id("java")
	id("dev.nokee.jni-library")
	id("dev.nokee.cpp-language")
}


library {
	targetMachines.set(listOf(
		machines.windows.x86_64,
		machines.macOS.x86_64,
		machines.linux.x86_64
	))
}
