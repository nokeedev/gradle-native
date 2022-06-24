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
package dev.nokee.fixtures

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.file.TestFile
import spock.lang.Ignore

abstract class AbstractNativeComponentBuildTypeFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def "can build each build type individually"() {
		given:
		makeSingleProjectWithDebugReleaseBuildTypes()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('assembleDebug')
		then:
		result.assertTasksExecutedAndNotSkipped(tasks.withBuildType('debug').allToAssemble)

		when:
		succeeds('assembleRelease')
		then:
		result.assertTasksExecutedAndNotSkipped(tasks.withBuildType('release').allToAssemble)
	}

	def "can build multiple build type in a single invocation"() {
		given:
		makeSingleProjectWithDebugReleaseBuildTypes()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('assembleDebug', 'assembleRelease')

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.withBuildType('debug').allToAssemble, tasks.withBuildType('release').allToAssemble)
	}

	def "defaults lifecycle build to debug"() {
		given:
		makeSingleProjectWithDebugReleaseBuildTypes()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('assemble')
		then:
		result.assertTasksExecutedAndNotSkipped(tasks.withBuildType('debug').allToLink, tasks.assemble)
	}

	@Ignore
	def "chooses a first defined build type"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << """
			${componentUnderTestDsl} {
				targetBuildTypes = [buildTypes.named('foo'), buildTypes.named('bar')]
			}
		"""

		when:
		succeeds('assemble')
		then:
		result.assertTasksExecutedAndNotSkipped(tasks.withBuildType('foo').allToLink, tasks.assemble)
	}

	def "can consume component without build types from component with build types"() {
		given:
		makeMultiProject()
		componentUnderTest.withImplementationAsSubproject('library').writeToProject(testDirectory)

		and:
		buildFile << """
			${componentUnderTestDsl} {
				targetBuildTypes = [buildTypes.named('debug'), buildTypes.named('release')]
			}
		"""

		when:
		succeeds(':assemble')

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.withBuildType('debug').allToLink, tasks(':library').allToLink, tasks.assemble)
	}

	def "can consume dependencies with matching build types"() {
		given:
		makeMultiProject()
		componentUnderTest.withImplementationAsSubproject('library').writeToProject(testDirectory)

		and:
		buildFile << """
			${componentUnderTestDsl} {
				targetBuildTypes = [buildTypes.named('debug'), buildTypes.named('release')]
			}
		"""
		file('library', buildFileName) << """
			library {
				targetBuildTypes = [buildTypes.named('debug'), buildTypes.named('release')]
			}
		"""

		when:
		succeeds(':assembleRelease')

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.withBuildType('release').allToAssemble, tasks(':library').withBuildType('release').allToLink)
	}

	@Ignore('Default to debug')
	def "can consume component with build types from component without build types"() {
		given:
		makeMultiProject()
		componentUnderTest.withImplementationAsSubproject('library').writeToProject(testDirectory)

		and:
		file('library', buildFileName) << """
			library {
				targetBuildTypes = [buildTypes.named('debug'), buildTypes.named('release')]
			}
		"""

		when:
		succeeds(':assemble')

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.allToLink, tasks(':library').withBuildType('debug').allToLink, tasks.assemble)
	}

	def "can choose which build type to consume"() {
		given:
		makeMultiProjectWithExplicitBuildTypeDependency('release')
		componentUnderTest.withImplementationAsSubproject('library').writeToProject(testDirectory)

		and:
		file('library', buildFileName) << """
			library {
				targetBuildTypes = [buildTypes.named('debug'), buildTypes.named('release')]
			}
		"""

		when:
		def result = succeeds(':assemble')

		then:
		result.assertTasksExecutedAndNotSkipped(tasks.allToLink, tasks(':library').withBuildType('release').allToLink, tasks.assemble)
	}

	def "can configure specific build type"() {
		given:
		makeSingleProjectWithDebugReleaseBuildTypes()
		componentUnderTest.withPreprocessorImplementation().writeToProject(testDirectory)

		and:
		buildFile << """
			${componentUnderTestDsl} {
				variants.configureEach({ it.buildVariant.hasAxisOf(buildTypes.named('debug')) }) {
					binaries.configureEach {
						compileTasks.configureEach {
							compilerArgs.add('-DSAY_HELLO_EVA')
						}
					}
				}
			}
		"""

		when:
		def resultDebug = succeeds(tasks.withBuildType('debug').objects, '-i')
		then:
		resultDebug.assertOutputContains('Bonjour, Eva!')

		when:
		def resultRelease = succeeds(tasks.withBuildType('release').objects, '-i')
		then:
		resultRelease.assertNotOutput('Bonjour, Eva!')
	}
//
//	def "can consume build with multiple build type"() {
//		given:
//		makeSingleProject()
//		componentUnderTest.writeToProject(testDirectory)
//
//		and:
//		buildFile << """
//			${componentUnderTestDsl} {
//				variants.configureEach({ it.buildVariant.hasAxisOf(buildTypes.named('debug')) }) {
//
//				}
//			}
//		"""
//	}

	// TODO: Can define dependencies per build type
	// TODO: Can identify which build type is build configured (from variant, binary, tasks)
	// TODO: Can configure tasks per build type (debug flags vs release flags)
	// TODO: Built binaries are in their own folder (and object)

	// TODO: Consuming a build without build type from build with build type (default one in a multi build type)
	// TODO: Consuming a build from Gradle native
	// TODO: Consuming a build with build type from build without build type

	// TODO: Support build type for test suite
	// TODO: Support build type in JNI
	// TODO: Support build type in iOS
	// TODO: Support build type in build adapter
	// TODO: Support build type in Visual Studio IDE
	// TODO: Support build type in Xcode IDE.

	protected abstract void makeSingleProject()

	protected void makeMultiProject() {
		makeMultiProjectWithoutDependency()
		buildFile << """
			${componentUnderTestDsl} {
				dependencies {
					implementation project(':library')
				}
			}
		"""
	}

	protected void makeMultiProjectWithExplicitBuildTypeDependency(String buildTypeNameToConsume) {
		makeMultiProjectWithoutDependency()
		buildFile << """
			${componentUnderTestDsl} {
				dependencies {
					implementation(project(':library')) {
						attributes {
							attribute(Attribute.of('dev.nokee.buildType', String), '${buildTypeNameToConsume}')
						}
					}
				}
			}
		"""
	}

	protected abstract void makeMultiProjectWithoutDependency()

	protected void makeSingleProjectWithDebugReleaseBuildTypes() {
		makeSingleProject()
		buildFile << """
			${componentUnderTestDsl} {
				targetBuildTypes = [buildTypes.named('debug'), buildTypes.named('release')]
			}
		"""
	}

	protected abstract ComponentUnderTest getComponentUnderTest()

	protected String getComponentUnderTestDsl() {
		if (this.getClass().simpleName.contains('Application')) {
			return 'application'
		} else if (this.getClass().simpleName.contains('Library')) {
			return 'library'
		}
		throw new UnsupportedOperationException('Please override getComponentUnderTestDsl()')
	}

	interface ComponentUnderTest {
		void writeToProject(TestFile projectDirectory)

		SourceElement withImplementationAsSubproject(String subprojectPath)

		SourceElement withPreprocessorImplementation()
	}
}
