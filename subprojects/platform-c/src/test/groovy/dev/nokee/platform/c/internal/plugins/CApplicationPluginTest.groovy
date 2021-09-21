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
package dev.nokee.platform.c.internal.plugins

import dev.nokee.fixtures.*
import dev.nokee.platform.base.Variant
import dev.nokee.platform.c.CApplication
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.NativeApplication
import org.gradle.api.Project
import spock.lang.Subject

trait CApplicationPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.c-application'
	}

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: pluginId
	}

	def getExtensionUnderTest() {
		return projectUnderTest.application
	}

	String getExtensionNameUnderTest() {
		return 'application'
	}

	Class getExtensionType() {
		return CApplication
	}

	Class getVariantType() {
		return NativeApplication
	}

	String[] getExpectedVariantAwareTaskNames() {
		return ['objects', 'executable']
	}

	void configureMultipleVariants() {
		extensionUnderTest.targetMachines = [extensionUnderTest.machines.macOS, extensionUnderTest.machines.windows, extensionUnderTest.machines.linux]
	}
}

@Subject(CApplicationPlugin)
class CApplicationPluginTest extends AbstractPluginTest implements CApplicationPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Subject(CApplicationPlugin)
class CApplicationTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements CApplicationPluginTestFixture {
}

@Subject(CApplicationPlugin)
class CApplicationTaskPluginTest extends AbstractTaskPluginTest implements CApplicationPluginTestFixture {
}

@Subject(CApplicationPlugin)
class CApplicationVariantPluginTest extends AbstractVariantPluginTest implements CApplicationPluginTestFixture {
}

@Subject(CApplicationPlugin)
class CApplicationBinaryPluginTest extends AbstractBinaryPluginTest implements CApplicationPluginTestFixture {
	@Override
	boolean hasExpectedBinaries(Variant variant) {
		variant.binaries.get().with { binaries ->
			assert binaries.size() == 1
			assert binaries.any { it instanceof ExecutableBinary }
		}
		return true
	}

	@Override
	boolean hasExpectedBinaries(Object extension) {
		if (extension.targetMachines.get().size() == 1) {
			extension.binaries.get().with { binaries ->
				assert binaries.size() == 1
				assert binaries.any { it instanceof ExecutableBinary }
			}
		} else {
			extension.binaries.get().with { binaries ->
				assert binaries.size() == 3
				assert binaries.count { it instanceof ExecutableBinary } == 3
			}
		}
		return true
	}
}
