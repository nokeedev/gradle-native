/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.swift.internal.plugins

import dev.nokee.fixtures.*
import dev.nokee.platform.base.Variant
import dev.nokee.platform.nativebase.NativeLibrary
import dev.nokee.platform.nativebase.SharedLibraryBinary
import dev.nokee.platform.swift.SwiftLibrary
import org.gradle.api.Project
import spock.lang.Subject

trait SwiftLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.swift-library'
	}

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: pluginId
	}

	def getExtensionUnderTest() {
		return projectUnderTest.library
	}

	String getExtensionNameUnderTest() {
		return 'library'
	}

	Class getExtensionType() {
		return SwiftLibrary
	}

	Class getVariantType() {
		return NativeLibrary
	}

	String[] getExpectedVariantAwareTaskNames() {
		return ['objects', 'sharedLibrary']
	}

	void configureMultipleVariants() {
		extensionUnderTest.targetMachines = [extensionUnderTest.machines.macOS, extensionUnderTest.machines.windows, extensionUnderTest.machines.linux]
	}
}

@Subject(SwiftLibraryPlugin)
class SwiftLibraryPluginTest extends AbstractPluginTest implements SwiftLibraryPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Subject(SwiftLibraryPlugin)
class SwiftLibraryTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements SwiftLibraryPluginTestFixture {
}

@Subject(SwiftLibraryPlugin)
class SwiftLibraryTaskPluginTest extends AbstractTaskPluginTest implements SwiftLibraryPluginTestFixture {
}

@Subject(SwiftLibraryPlugin)
class SwiftLibraryVariantPluginTest extends AbstractVariantPluginTest implements SwiftLibraryPluginTestFixture {
}

@Subject(SwiftLibraryPlugin)
class SwiftLibraryBinaryPluginTest extends AbstractBinaryPluginTest implements SwiftLibraryPluginTestFixture {
	@Override
	boolean hasExpectedBinaries(Variant variant) {
		variant.binaries.get().with { binaries ->
			assert binaries.size() == 1
			assert binaries.any { it instanceof SharedLibraryBinary }
		}
		return true
	}

	@Override
	boolean hasExpectedBinaries(Object extension) {
		if (extension.targetMachines.get().size() == 1) {
			extension.binaries.get().with { binaries ->
				assert binaries.size() == 1
				assert binaries.any { it instanceof SharedLibraryBinary }
			}
		} else {
			extension.binaries.get().with { binaries ->
				assert binaries.size() == 3
				assert binaries.count { it instanceof SharedLibraryBinary } == 3
			}
		}
		return true
	}
}
