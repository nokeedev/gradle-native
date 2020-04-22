package dev.nokee.platform.ios

import dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.platform.ios.fixtures.BundleFixture
import dev.nokee.platform.ios.fixtures.ObjectiveCIosApp
import org.apache.commons.lang3.SystemUtils
import spock.lang.Requires

@Requires({ SystemUtils.IS_OS_MAC })
@RequiresInstalledToolChain(value = ToolChainRequirement.CLANG)
class BasicIosApplicationFunctionalTest extends AbstractFunctionalSpec {
	def "can assemble"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('assemble')
		then:
		result.assertTasksExecutedAndNotSkipped(':compileMainExecutableMainObjc', ':linkMainExecutable', ':mainExecutable',
			':compileStoryboard', ':linkStoryboard', ':compileAssetCatalog', ':createApplicationBundle', ':processPropertyList', ':signApplicationBundle',
			':assemble')
		bundle('build/ios/products/main/Application.app').assertHasDescendants('Application', 'Base.lproj/LaunchScreen.storyboardc', 'Base.lproj/Main.storyboardc', 'Info.plist', '_CodeSignature/CodeResources')
		// TODO: Check that it's signed
		// TODO: Check what is the target of the app
	}

	BundleFixture bundle(String path) {
		return new BundleFixture(file(path))
	}

	void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.ios-application'
				id 'dev.nokee.objective-c-language'
			}
		'''
		settingsFile << "rootProject.name = 'application'"
	}

	ObjectiveCIosApp getComponentUnderTest() {
		return new ObjectiveCIosApp()
	}
}
