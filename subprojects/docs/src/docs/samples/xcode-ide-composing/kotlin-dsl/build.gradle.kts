import dev.nokee.language.nativebase.tasks.NativeSourceCompile
import dev.nokee.platform.jni.JvmJarBinary
import dev.nokee.platform.nativebase.SharedLibraryBinary

plugins {
	id("java")
	id("dev.nokee.jni-library")
	id("dev.nokee.objective-c-language")
	id("dev.nokee.xcode-ide-base")
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
					it.operatingSystem.isFreeBSD -> listOf("-L/usr/local/lib")
					else -> emptyList()
				}
			})
		}
	}
}

//region Helper methods
fun getJvmBinary(): Provider<JvmJarBinary> {
	return library.binaries.withType(JvmJarBinary::class.java).elements.map { it.first() }
}

fun getSharedLibraryBinary(): Provider<SharedLibraryBinary> {
	return library.binaries.withType(SharedLibraryBinary::class.java).elements.map { it.first() }
}

fun asHeaderSearchPaths(): (SharedLibraryBinary) -> String {
	return {
		it.compileTasks.withType(NativeSourceCompile::class.java).get().first().headerSearchPaths.get().map { "\"${it.asFile.absolutePath}\"" }.joinToString(" ")
	}
}
//endregion

xcode {
	projects.register("jni-greeter") {
		targets.register("JniJar") {
			productReference.set(getJvmBinary().flatMap { it.jarTask.get().archiveFileName })
			productType.set(productTypes.of("com.apple.product-type.library.java.archive"))
			buildConfigurations.register("Default") {
				productLocation.set(getJvmBinary().flatMap { it.jarTask.get().archiveFile })
				buildSettings.put("PRODUCT_NAME", ideTarget.productName)
			}
			sources.from(fileTree("src/main/java") { include("**/*.java") })
		}
		targets.register("JniSharedLibrary") {
			productReference.set(getSharedLibraryBinary().map { it.linkTask.get().linkedFile.get().asFile.getName() })
			productType.set(productTypes.dynamicLibrary)
			buildConfigurations.register("Default") {
				productLocation.set(getSharedLibraryBinary().flatMap { it.linkTask.get().linkedFile })
				buildSettings.put("HEADER_SEARCH_PATHS", getSharedLibraryBinary().map(asHeaderSearchPaths()))
				buildSettings.put("PRODUCT_NAME", ideTarget.productName)
			}
			sources.from(fileTree("src/main/objc") { include("**/*.m") })
			sources.from(fileTree("src/main/headers") { include("**/*.h") })
		}
	}
}
