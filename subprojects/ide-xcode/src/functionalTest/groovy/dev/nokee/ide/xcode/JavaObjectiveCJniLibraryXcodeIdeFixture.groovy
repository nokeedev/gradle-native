package dev.nokee.ide.xcode

import dev.nokee.platform.jni.JvmJarBinary
import dev.nokee.platform.jni.fixtures.JavaJniObjectiveCGreeterLib
import dev.nokee.platform.nativebase.SharedLibraryBinary
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal

trait JavaObjectiveCJniLibraryXcodeIdeFixture {
	String registerJniGreeterTarget() {
		return """
			targets.register('JniGreeter') {
				productReference = library.binaries.withType(${JvmJarBinary.simpleName}).elements.flatMap { it.first().jarTask.get().archiveFileName }
				productType = productTypes.of('com.apple.product-type.library.java.archive')
				buildConfigurations.register('Default') {
					productLocation = library.binaries.withType(${JvmJarBinary.simpleName}).elements.flatMap { it.first().jarTask.get().archiveFile }
					buildSettings.put('PRODUCT_NAME', ideTarget.productName)
				}
				sources.from(fileTree('src/main/java') { include('**/*.java') })
			}
		"""
	}

	String registerJniSharedLibraryTarget() {
		return """
			targets.register('JniSharedLibrary') {
				productReference = library.binaries.withType(${SharedLibraryBinary.simpleName}).elements.map { it.first().linkTask.get().linkedFile.get().asFile.getName() }
				productType = productTypes.dynamicLibrary
				buildConfigurations.register('Default') {
					productLocation = library.binaries.withType(${SharedLibraryBinary.simpleName}).elements.flatMap { it.first().linkTask.get().linkedFile }
					buildSettings.put('HEADER_SEARCH_PATHS', library.binaries.withType(${SharedLibraryBinaryInternal.simpleName}).elements.flatMap { it.first().headerSearchPaths.map { it.collect { "\\"\${it.asFile.absolutePath}\\"" }.join(' ') } })
					buildSettings.put('PRODUCT_NAME', ideTarget.productName)
				}
				sources.from(fileTree('src/main/objc') { include('**/*.m') })
				sources.from(fileTree('src/main/headers') { include('**/*.h') })
			}
		"""
	}

	JavaJniObjectiveCGreeterLib getComponent() {
		return new JavaJniObjectiveCGreeterLib()
	}
}
