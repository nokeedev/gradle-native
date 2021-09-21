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
package dev.nokee.platform.ios.internal.plugins

import dev.nokee.fixtures.AbstractBinaryPluginTest
import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.fixtures.AbstractTaskPluginTest
import dev.nokee.fixtures.AbstractVariantPluginTest
import dev.nokee.platform.base.Variant
import dev.nokee.platform.ios.IosApplication
import dev.nokee.platform.ios.SwiftIosApplicationExtension
import dev.nokee.platform.nativebase.NativeComponentDependencies
import org.apache.commons.lang3.SystemUtils
import dev.nokee.platform.ios.internal.IosApplicationBundleInternal
import dev.nokee.platform.ios.internal.SignedIosApplicationBundleInternal
import dev.nokee.platform.nativebase.ExecutableBinary
import org.gradle.api.Project
import spock.lang.Requires
import spock.lang.Subject

import static org.junit.Assume.assumeTrue

trait SwiftIosApplicationPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.swift-ios-application'
	}

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: pluginId
	}

	def getExtensionUnderTest() {
		return projectUnderTest.application
	}

	Class getExtensionType() {
		return SwiftIosApplicationExtension
	}

	Class getDependenciesType() {
		return NativeComponentDependencies
	}

	Class getVariantType() {
		return IosApplication
	}

	String[] getExpectedVariantAwareTaskNames() {
		return ['objects', 'bundle', 'compileAssetCatalog', 'compileStoryboard', 'createApplicationBundle', 'linkStoryboard', 'processPropertyList', 'signApplicationBundle']
	}

	void configureMultipleVariants() {
		assumeTrue(false)
	}
}

@Requires({ SystemUtils.IS_OS_MAC})
@Subject(SwiftIosApplicationPlugin)
class SwiftIosApplicationPluginTest extends AbstractPluginTest implements SwiftIosApplicationPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Requires({SystemUtils.IS_OS_MAC})
@Subject(SwiftIosApplicationPlugin)
class SwiftIosApplicationTaskPluginTest extends AbstractTaskPluginTest implements SwiftIosApplicationPluginTestFixture {
}

@Subject(SwiftIosApplicationPlugin)
class SwiftIosApplicationVariantPluginTest extends AbstractVariantPluginTest implements SwiftIosApplicationPluginTestFixture {
}

@Subject(SwiftIosApplicationPlugin)
class SwiftIosApplicationBinaryPluginTest extends AbstractBinaryPluginTest implements SwiftIosApplicationPluginTestFixture {
	@Override
	boolean hasExpectedBinaries(Variant variant) {
		variant.binaries.get().with { binaries ->
			assert binaries.size() == 3
			assert binaries.any { it instanceof ExecutableBinary }
			assert binaries.any { it instanceof IosApplicationBundleInternal }
			assert binaries.any { it instanceof SignedIosApplicationBundleInternal }
		}
		return true
	}

	@Override
	boolean hasExpectedBinaries(Object extension) {
		extension.binaries.get().with { binaries ->
			assert binaries.size() == 3
			assert binaries.any { it instanceof ExecutableBinary }
			assert binaries.any { it instanceof IosApplicationBundleInternal }
			assert binaries.any { it instanceof SignedIosApplicationBundleInternal }
		}
		return true
	}
}
