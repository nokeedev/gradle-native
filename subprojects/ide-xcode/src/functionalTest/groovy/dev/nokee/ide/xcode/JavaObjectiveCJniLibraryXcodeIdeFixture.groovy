/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.ide.xcode

import dev.nokee.platform.jni.JvmJarBinary
import dev.nokee.platform.jni.fixtures.JavaJniObjectiveCGreeterLib
import dev.nokee.platform.nativebase.SharedLibraryBinary

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
				ideProject.groups.register('JniGreeter') {
					sources.from(fileTree('src/main/java') { include('**/*.java') })
				}
			}
		"""
	}

	String registerJniSharedLibraryTarget() {
		return """
			targets.register('JniSharedLibrary') {
				productReference = library.binaries.withType(${SharedLibraryBinary.simpleName}).elements.flatMap { binaries -> provider { binaries.first().linkTask.get().linkedFile.get().asFile.name } }
				productType = productTypes.dynamicLibrary
				buildConfigurations.register('Default') {
					productLocation = library.binaries.withType(${SharedLibraryBinary.simpleName}).elements.flatMap { it.first().linkTask.get().linkedFile }
					buildSettings.put('HEADER_SEARCH_PATHS', library.binaries.withType(${SharedLibraryBinary.simpleName}).elements.flatMap { it.first().headerSearchPaths.map { it.collect { "\\"\${it.asFile.absolutePath}\\"" }.join(' ') } })
					buildSettings.put('PRODUCT_NAME', ideTarget.productName)
				}
				sources.from(fileTree('src/main/objc') { include('**/*.m') })
				sources.from(fileTree('src/main/headers') { include('**/*.h') })
				ideProject.groups.register('JniSharedLibrary') {
					sources.from(fileTree('src/main/objc') { include('**/*.m') })
					sources.from(fileTree('src/main/headers') { include('**/*.h') })
				}
			}
		"""
	}

	JavaJniObjectiveCGreeterLib getComponent() {
		return new JavaJniObjectiveCGreeterLib('jni-greeter')
	}
}
