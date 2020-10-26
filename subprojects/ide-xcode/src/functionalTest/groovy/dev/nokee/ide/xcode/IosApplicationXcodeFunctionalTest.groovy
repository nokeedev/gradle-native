package dev.nokee.ide.xcode

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.gradleplugins.test.fixtures.gradle.executer.internal.AbstractGradleDistribution
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.SourceFile
import dev.nokee.platform.ios.fixtures.ObjectiveCIosApp
import org.apache.commons.lang3.SystemUtils
import spock.lang.Requires

import static org.junit.Assume.assumeTrue

abstract class IosApplicationXcodeFunctionalTest extends AbstractGradleSpecification implements XcodeIdeFixture {
	def "adds the Xcode project to the workspace"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('xcode')

		then:
		xcodeWorkspace('app').assertHasProjects('app')
	}

	def "can create Xcode project with sources"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('xcode')

		then:
		xcodeProject('app').assertHasSourceLayout(*expectedSourceLayoutOfComponentUnderTest, 'build.gradle', 'settings.gradle')
	}

	def "creates Xcode project with a scheme matching the target"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('xcode')

		then:
		xcodeProject('app').assertHasTarget('App')
		xcodeProject('app').assertHasSchemes('App')
	}

	def "creates Xcode project with default build configuration"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('xcode')

		then:
		xcodeProject('app').assertHasBuildConfigurations('Default')
	}

	def "creates Xcode project with iOS application target delegating to Gradle"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('xcode')

		then:
		def xcodeProject = xcodeProject('app')
		xcodeProject.assertHasTarget('App')
		xcodeProject.getTargetByName('App').productReference.name == 'App.app'
		xcodeProject.getTargetByName('App').productType == 'com.apple.product-type.application'

		and:
		xcodeProject.targets.first().assertTargetDelegateToGradle()
	}

	def "creates Xcode project with iOS application target indexer"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('xcode')

		then:
		def xcodeProject = xcodeProject('app')
		xcodeProject.assertHasTarget('__indexer_App')
		xcodeProject.getTargetByName('__indexer_App').productReference.name == 'App.app'
		xcodeProject.getTargetByName('__indexer_App').productType == 'dev.nokee.product-type.indexer'

		and:
		xcodeProject.targets.first().assertTargetDelegateToGradle()
	}

	def "xcodebuild sees the scheme of the target"() {
		using xcodebuildTool
		settingsFile << configurePluginClasspathAsBuildScriptDependencies()
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('xcode')

		then:
		def result = xcodebuild.withWorkspace(xcodeWorkspace('app.xcworkspace')).withArgument('-list').execute()
		result.standardOutput.asString.contains('''Information about workspace "app":
			|    Schemes:
			|        App'''.stripMargin())
	}

	@Requires({ SystemUtils.IS_OS_MAC })
	def "can build iOS application from Xcode IDE"() {
		assumeTrue('iOS application does not support no source', hasSourcesToBuild())

		using xcodebuildTool
		settingsFile << configurePluginClasspathAsBuildScriptDependencies()
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':appXcodeProject', ':xcodeWorkspace', ':xcode')

		and:
		def result = xcodebuild.withWorkspace(xcodeWorkspace('app.xcworkspace')).withScheme("App").succeeds()
		result.assertTasksExecutedAndNotSkipped(':compileAssetCatalog', ':compileStoryboard', ':linkStoryboard', ':processPropertyList', ':compileObjectiveC', ':link', ':createApplicationBundle', ':signApplicationBundle', ':_xcode___app_App_Default')
	}

	@Requires({ SystemUtils.IS_OS_MAC })
	def "can build iOS application from Xcode IDE multiple time"() {
		assumeTrue('iOS application does not support no source', hasSourcesToBuild())

		using xcodebuildTool
		settingsFile << configurePluginClasspathAsBuildScriptDependencies()
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		succeeds('xcode')

		when:
		def xcodeResult1 = xcodebuild.withWorkspace(xcodeWorkspace('app.xcworkspace')).withScheme("App").succeeds()
		then:
		xcodeResult1.assertTasksExecutedAndNotSkipped(':compileAssetCatalog', ':compileStoryboard', ':linkStoryboard', ':processPropertyList', ':compileObjectiveC', ':link', ':createApplicationBundle', ':signApplicationBundle', ':_xcode___app_App_Default')

		when:
		def xcodeResult2 = xcodebuild.withWorkspace(xcodeWorkspace('app.xcworkspace')).withScheme("App").succeeds()
		then:
		xcodeResult2.assertTasksSkipped(':compileAssetCatalog', ':compileStoryboard', ':linkStoryboard', ':processPropertyList', ':compileObjectiveC', ':link', ':createApplicationBundle', ':signApplicationBundle', ':_xcode___app_App_Default')
	}

	protected abstract List<String> getExpectedSourceLayoutOfComponentUnderTest();

	// TODO: Remove once the iOS application plugin support no source
	protected abstract boolean hasSourcesToBuild()

	protected abstract void makeSingleProject()

	protected abstract SourceElement getComponentUnderTest()
}

@Requires({ SystemUtils.IS_OS_MAC }) // TODO: It should create unbuildable targets
class NoSourceIosApplicationXcodeIdeFunctionalTest extends IosApplicationXcodeFunctionalTest {
	protected SourceElement getComponentUnderTest() {
		return new SourceElement() {
			final List<SourceFile> files = Collections.emptyList()
		}
	}

	protected void makeSingleProject() {
		settingsFile << "rootProject.name = 'app'"
		buildFile << """
			plugins {
				id 'dev.nokee.xcode-ide'
				id 'dev.nokee.objective-c-ios-application'
			}
		"""
	}

	@Override
	protected boolean hasSourcesToBuild() {
		return false
	}

	@Override
	protected List<String> getExpectedSourceLayoutOfComponentUnderTest() {
		return ['Products/App.app']
	}
}

@Requires({ SystemUtils.IS_OS_MAC }) // TODO: It should create unbuildable targets
class ObjectiveCIosApplicationXcodeIdeFunctionalTest extends IosApplicationXcodeFunctionalTest {
	protected ObjectiveCIosApp getComponentUnderTest() {
		return new ObjectiveCIosApp()
	}

	protected void makeSingleProject() {
		settingsFile << "rootProject.name = 'app'"
		buildFile << """
			plugins {
				id 'dev.nokee.xcode-ide'
				id 'dev.nokee.objective-c-ios-application'
			}
		"""
	}

	@Override
	protected boolean hasSourcesToBuild() {
		return true
	}

	@Override
	protected List<String> getExpectedSourceLayoutOfComponentUnderTest() {
		return ['Products/App.app', 'App/AppDelegate.h', 'App/AppDelegate.m', 'App/SceneDelegate.h', 'App/SceneDelegate.m', 'App/ViewController.h', 'App/ViewController.m', 'App/Main.storyboard', 'App/Assets.xcassets', 'App/LaunchScreen.storyboard', 'App/Info.plist', 'App/main.m']
	}
}
