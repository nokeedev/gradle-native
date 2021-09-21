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
package dev.nokee.platform.ios

import dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.platform.ios.fixtures.BundleFixture
import dev.nokee.platform.ios.fixtures.ObjectiveCIosApp
import spock.lang.Requires

@Requires({ os.macOs })
@RequiresInstalledToolChain(value = ToolChainRequirement.CLANG)
class BasicIosApplicationFunctionalTest extends AbstractFunctionalSpec {
	def "can assemble"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('assemble')
		then:
		result.assertTasksExecutedAndNotSkipped(':compileObjectiveC', ':link',
			':compileStoryboard', ':linkStoryboard', ':compileAssetCatalog', ':createApplicationBundle', ':processPropertyList', ':signApplicationBundle',
			':assemble')
		bundle('build/ios/products/main/Application.app').assertHasDescendants('Application', 'Assets.car', 'Base.lproj/LaunchScreen.storyboardc', 'Base.lproj/Main.storyboardc', 'Info.plist', 'PkgInfo', '_CodeSignature/CodeResources')
		// TODO: Check that it's signed
		// TODO: Check what is the target of the app
	}

	BundleFixture bundle(String path) {
		return new BundleFixture(file(path))
	}

	void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.objective-c-ios-application'
			}
		'''
		settingsFile << "rootProject.name = 'application'"
	}

	ObjectiveCIosApp getComponentUnderTest() {
		return new ObjectiveCIosApp()
	}
}
