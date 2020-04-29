plugins {
	id("java")
	id("dev.nokee.jni-library")
	id("dev.nokee.cpp-language")
}

import dev.nokee.platform.jni.JarBinary
import dev.nokee.platform.jni.JvmJarBinary
import dev.nokee.platform.jni.JniJarBinary

library {
	targetMachines.set(listOf(machines.macOS, machines.linux, machines.windows))
}

/**
 * Returns a transfomer that convert a list of {@link JniJarBinary} or {@link JvmJarBinary} to a list of FileTree
 * representing the JAR content.
 *
 * @return a transformer of {@link JarBinary} instances to {@link FileTree} of their JAR content.
 */
fun asZipTrees(): (Set<JarBinary>) -> List<Provider<FileTree>> {
	return { jarBinaries: Set<JarBinary> ->
		jarBinaries.map { it.jarTask.map { zipTree(it.archiveFile) } }
	}
}

tasks.register<Jar>("uberJar") {
	from(library.binaries.withType(JniJarBinary::class.java).elements.map(asZipTrees())) { // <1>
		exclude("META-INF/**")
	}
	from(library.binaries.withType(JvmJarBinary::class.java).elements.map(asZipTrees()))   // <2>
	archiveClassifier.set("uber")
}
