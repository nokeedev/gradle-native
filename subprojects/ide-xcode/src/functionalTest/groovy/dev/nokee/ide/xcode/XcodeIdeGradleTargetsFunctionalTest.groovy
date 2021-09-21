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

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import spock.lang.Unroll

class XcodeIdeGradleTargetsFunctionalTest extends AbstractGradleSpecification implements XcodeIdeFixture {
	protected void makeProjectWithSubproject() {
		settingsFile << """
			rootProject.name = 'foo'
			include 'bar'
		"""
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('foo')
		file('bar/build.gradle') << applyXcodeIdePlugin() << configureXcodeIdeProject('bar')
	}

	def "includes init script from the command line in all Xcode Gradle target command line"() {
		makeProjectWithSubproject()
		def initFile = file('init.gradle')
		initFile.createNewFile()

		when:
		executer = executer.usingInitScript(initFile)
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':bar:barXcodeProject', ':bar:xcode', ':fooXcodeProject', ':xcodeWorkspace', ':xcode')
		xcodeProject('foo').getTargetByName('Foo').assertTargetDelegateToGradle()
		xcodeProject('foo').getTargetByName('Foo').buildArgumentsString.contains("--init-script \"${initFile}\"")
		xcodeProject('bar/bar').getTargetByName('Bar').buildArgumentsString.contains("--init-script \"${initFile}\"")
	}

	@Unroll
	def "delegates to Gradle for all product types (#productType)"(productType) {
		given:
		settingsFile << "rootProject.name = 'foo'"
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('foo')

		when:
		succeeds('xcode')

		then:
		xcodeProject('foo').getTargetByName('Foo').assertTargetDelegateToGradle()

		where:
		productType << XcodeIdeProductTypes.getKnownValues()
	}
}
