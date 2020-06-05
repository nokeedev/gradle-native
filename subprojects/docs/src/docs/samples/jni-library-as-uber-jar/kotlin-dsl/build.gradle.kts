plugins {
	id("java")
	id("dev.nokee.jni-library")
	id("dev.nokee.cpp-language")
}

import dev.nokee.platform.jni.JarBinary
import dev.nokee.platform.jni.JvmJarBinary
import dev.nokee.platform.jni.JniJarBinary
import dev.nokee.platform.nativebase.TargetMachine

library {
	targetMachines.set(listOf(machines.macOS, machines.linux, machines.windows))
}

/**
 * Returns a transfomer that convert a list of {@link JniJarBinary} or {@link JvmJarBinary} to a list of {@link FileTree}
 * representing the JAR content.
 *
 * @return a transformer of {@link JarBinary} instances to {@link FileTree} of their JAR content.
 */
fun asZipTree(): (JarBinary) -> Provider<FileTree> {
	return { jarBinary: JarBinary ->
		jarBinary.jarTask.map { zipTree(it.archiveFile) }
	}
}

fun isHostTargeted(targetMachine: TargetMachine): Boolean {
	val osName = System.getProperty("os.name").toLowerCase().replace(" ", "")
	val osFamily = targetMachine.operatingSystemFamily
	if (osFamily.isWindows && osName.contains("windows")) {
		return true
	} else if (osFamily.isLinux && osName.contains("linux")) {
		return true
	} else if (osFamily.isMacOs && osName.contains("macos")) {
		return true
	}
	return false
}

tasks.register<Jar>("uberJar") {
	from(library.variants.flatMap { variant ->
		val result = ArrayList<Provider<List<Provider<FileTree>>>>()
		if (isHostTargeted(variant.targetMachine)) {
			result.add(variant.binaries.withType(JniJarBinary::class.java).map(asZipTree()))
		}
		result
	}) { // <1>
		exclude("META-INF/**")
	}
	from(library.binaries.withType(JvmJarBinary::class.java).map(asZipTree()))   // <2>
	archiveClassifier.set("uber")
}
