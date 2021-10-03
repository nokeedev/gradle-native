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
package dev.nokee.platform.ios.internal.plugins

import dev.nokee.internal.testing.util.ProjectTestUtils
import dev.nokee.fixtures.AbstractBinaryPluginTest
import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.fixtures.AbstractTaskPluginTest
import dev.nokee.fixtures.AbstractVariantPluginTest
import dev.nokee.platform.base.Variant
import dev.nokee.platform.ios.IosApplication
import dev.nokee.platform.ios.ObjectiveCIosApplicationExtension
import dev.nokee.platform.ios.internal.IosApplicationBundleInternal
import dev.nokee.platform.ios.internal.SignedIosApplicationBundleInternal
import dev.nokee.platform.ios.tasks.internal.AssetCatalogCompileTask
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask
import dev.nokee.platform.ios.tasks.internal.StoryboardCompileTask
import dev.nokee.platform.ios.tasks.internal.StoryboardLinkTask
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.NativeComponentDependencies
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.Project
import spock.lang.Requires
import spock.lang.Specification
import spock.lang.Subject

import static org.junit.Assume.assumeTrue

trait ObjectiveCIosApplicationPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.objective-c-ios-application'
	}

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: pluginId
	}

	def getExtensionUnderTest() {
		return projectUnderTest.application
	}

	Class getExtensionType() {
		return ObjectiveCIosApplicationExtension
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

@Requires({SystemUtils.IS_OS_MAC})
@Subject(ObjectiveCIosApplicationPlugin)
class ObjectiveCIosApplicationPluginLayoutTest extends AbstractPluginTest implements ObjectiveCIosApplicationPluginTestFixture {
	final String pluginIdUnderTest = pluginId

	@Override
	def getExtensionUnderTest() {
		return projectUnderTest.application
	}

	@Override
	Class getExtensionType() {
		return ObjectiveCIosApplicationExtension
	}
}

@Subject(ObjectiveCIosApplicationPlugin)
class ObjectiveCIosApplicationTaskPluginTest extends AbstractTaskPluginTest implements ObjectiveCIosApplicationPluginTestFixture {
}

@Requires({SystemUtils.IS_OS_MAC})
@Subject(ObjectiveCIosApplicationPlugin)
class ObjectiveCIosApplicationPluginTest extends Specification {
	def project = ProjectTestUtils.rootProject()

	def "applies the lifecycle-base plugin"() {
		when:
		project.apply plugin: 'dev.nokee.objective-c-ios-application'

		then:
		project.pluginManager.hasPlugin('lifecycle-base')
	}

	def "creates storyboard build tasks"() {
		given:
		project.apply plugin: 'dev.nokee.objective-c-ios-application'

		when:
		project.evaluate()
		project.application.variants.get()

		then:
		project.tasks.withType(StoryboardCompileTask).size() == 1

		and:
		project.tasks.withType(StoryboardLinkTask).size() == 1
	}

	def "creates asset compilation tasks"() {
		given:
		project.apply plugin: 'dev.nokee.objective-c-ios-application'

		when:
		project.evaluate()
		project.application.variants.get()

		then:
		project.tasks.withType(AssetCatalogCompileTask).size() == 1
	}

	def "creates application bundle creation tasks"() {
		given:
		project.apply plugin: 'dev.nokee.objective-c-ios-application'

		when:
		project.evaluate()
		project.application.variants.get()

		then:
		project.tasks.withType(CreateIosApplicationBundleTask).size() == 1
	}
}

@Subject(ObjectiveCIosApplicationPlugin)
class ObjectiveCIosApplicationVariantPluginTest extends AbstractVariantPluginTest implements ObjectiveCIosApplicationPluginTestFixture {
}

@Subject(ObjectiveCIosApplicationPlugin)
class ObjectiveCIosApplicationBinaryPluginTest extends AbstractBinaryPluginTest implements ObjectiveCIosApplicationPluginTestFixture {
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
