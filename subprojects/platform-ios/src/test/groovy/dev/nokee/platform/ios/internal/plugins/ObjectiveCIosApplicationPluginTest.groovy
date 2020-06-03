package dev.nokee.platform.ios.internal.plugins

import dev.gradleplugins.spock.lang.CleanupTestDirectory
import dev.gradleplugins.spock.lang.TestNameTestDirectoryProvider
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
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.Project
import org.gradle.nativeplatform.NativeExecutable
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
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

	Class getVariantType() {
		return IosApplication
	}

	String[] getExpectedVariantAwareTaskNames() {
		return ['objects', 'bundle']
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
@CleanupTestDirectory
class ObjectiveCIosApplicationPluginTest extends Specification {
	@Rule
	private final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider(getClass())

	def project = ProjectBuilder.builder().withProjectDir(temporaryFolder.testDirectory).build()

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
