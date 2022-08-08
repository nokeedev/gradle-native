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
package dev.nokee.fixtures

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin
import dev.nokee.runtime.nativebase.OperatingSystemFamily
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.hamcrest.CoreMatchers
import spock.lang.Unroll

import static org.junit.Assume.assumeThat
import static org.junit.Assume.assumeTrue

abstract class AbstractNativeComponentDependenciesFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	protected String getLibraryProjectName() {
		return 'library'
	}

	def "can declare implementation dependencies on component"() {
		given:
		makeComponentWithLibrary()
		buildFile << """
			${componentUnderTestDsl} {
				dependencies {
					${implementationBucketNameUnderTest} ${dependencyNotation}
				}
			}
		"""

		when:
		run(':assemble')

		then:
		result.assertTasksExecuted(libraryTasks, taskNamesUnderTest.allToLifecycleAssemble)
	}

	def "can define implementation dependencies on each variant"() {
		assumeTrue(canDefineDependencyOnVariants())

		given:
		makeComponentWithLibrary()
		buildFile << """
			${componentUnderTestDsl} {
				variants.configureEach { b ->
					dependencies {
						${implementationBucketNameUnderTest} ${dependencyNotation}
					}
				}
			}
		"""

		when:
		run(':assemble')

		then:
		result.assertTasksExecuted(libraryTasks, taskNamesUnderTest.allToLifecycleAssemble)
	}

	// TODO: Variant-aware configuration
	def "can define implementation dependencies on single variant"() {
		assumeTrue(canDefineDependencyOnVariants())

		given:
		makeComponentWithLibrary()
		buildFile << configureToolChainSupport('foo') << """
			import ${OperatingSystemFamily.canonicalName}

			${componentUnderTestDsl} {
				targetMachines = [machines.${currentHostOperatingSystemFamilyDsl}, machines.os('foo')]
				variants.configureEach({it.buildVariant.getAxisValue(${OperatingSystemFamily.simpleName}.OPERATING_SYSTEM_COORDINATE_AXIS).${currentHostOperatingSystemFamilyDsl}}) {
					dependencies {
						${implementationBucketNameUnderTest} ${dependencyNotation}
					}
				}
			}
		"""

		when:
		run(taskNamesUnderTest.withOperatingSystemFamily(currentOsFamilyName).assemble, '--dry-run')
		then:
		// TODO: https://github.com/gradle-plugins/toolbox/issues/15
		(libraryTasks + taskNamesUnderTest.withOperatingSystemFamily(currentOsFamilyName).allToAssemble).each { result.assertOutputContains(it) }

		when:
		run(taskNamesUnderTest.withOperatingSystemFamily('foo').assemble, '--dry-run')
		then:
		// TODO: https://github.com/gradle-plugins/toolbox/issues/15
		taskNamesUnderTest.withOperatingSystemFamily('foo').allToAssemble.each { result.assertOutputContains(it) }
		if (!libraryTasks.empty) {
			libraryTasks.each { result.assertNotOutput(it) }
		}
	}

	@Unroll
	def "can declare implementation dependencies on component with different linkages"(linkages) {
		given:
		makeComponentWithLibrary()
		assumeThat("library project is a native project", file(libraryProjectName, buildFileName).text, CoreMatchers.containsString("id 'dev.nokee."))

		and:
		buildFile << """
			${componentUnderTestDsl} {
				dependencies {
					${implementationBucketNameUnderTest} ${dependencyNotation}
				}
			}
		"""

		and:
		file(libraryProjectName, buildFileName) << """
			library {
				targetLinkages = [${linkages}]
			}
		"""

		when:
		run(':assemble')

		then:
		result.assertTasksExecuted(allTasksToLinkLibrary, taskNamesUnderTest.allToLifecycleAssemble)

		where:
		linkages << ['linkages.static', 'linkages.shared', 'linkages.static, linkages.shared']
	}

	protected List<String> getAllTasksToLinkLibrary() {
		def libraryBuildFile = file(libraryProjectName, buildFileName)

		def libraryTasks = tasks(":${libraryProjectName}")
		if (libraryBuildFile.text.contains('[linkages.static, linkages.shared]')) {
			libraryTasks = libraryTasks.withLinkage('shared')
		}

		def result = libraryTasks.allToLinkElements
		if (libraryBuildFile.text.contains('[linkages.static]')) {
			result = libraryTasks.forStaticLibrary.allToLinkElements
		}

		if (this.class.name.contains('WithStaticLinkage')) {
			if (this.class.simpleName.startsWith('Swift')) {
				result = [libraryTasks.compile]
			} else {
				result = [libraryTasks.syncApiElements]
			}
		}

		return result
	}

	// TODO: Add test for source dependencies

	/**
	 * Creates a build with the component under test in the root project and a library in the 'lib' project.
	 */
	protected abstract void makeComponentWithLibrary()

	protected NativeProjectTasks getTaskNamesUnderTest() {
		tasks
	}

	protected abstract String getComponentUnderTestDsl()

	protected abstract List<String> getLibraryTasks()

	protected abstract String getDependencyNotation()

	protected String getImplementationBucketNameUnderTest() {
		return 'implementation'
	}

	protected boolean canDefineDependencyOnVariants() {
		return true
	}

	protected String configureToolChainSupport(String operatingSystem, String architecture = currentArchitecture) {
		String className = "ToolChainFor${operatingSystem.capitalize()}${architecture.capitalize()}Rules".replace('-', '')
		return """
			class ${className} extends RuleSource {
				@Finalize
				void addToolChain(NativeToolChainRegistry toolChains) {
					toolChains.create("toolChainFor${operatingSystem.capitalize()}${architecture.capitalize()}", Gcc) {
						path "/not/found"
						target("${operatingSystem}${architecture}") // It needs to be the same as NativePlatformFactory#platformNameFor
					}
				}
			}
			import ${NokeeStandardToolChainsPlugin.canonicalName}
			plugins.withType(NokeeStandardToolChainsPlugin) {
				// TODO: Applies after the Stardard tool chain plugin
				plugins.apply(${className})
			}
		"""
	}

	protected String getCurrentHostOperatingSystemFamilyDsl() {
		String osFamily = DefaultNativePlatform.getCurrentOperatingSystem().toFamilyName()
		if (osFamily == OperatingSystemFamily.MACOS) {
			return "macOS"
		} else {
			return osFamily
		}
	}
}


abstract class AbstractNativeComponentProjectDependenciesFunctionalTest extends AbstractNativeComponentDependenciesFunctionalTest {
	@Override
	protected String getDependencyNotation() {
		return "project(':${libraryProjectName}')"
	}

	protected String configureMultiProjectBuild() {
		return """
			include '${libraryProjectName}'
		"""
	}
}

abstract class AbstractNativeComponentIncludedBuildDependenciesFunctionalTest extends AbstractNativeComponentDependenciesFunctionalTest {
	@Override
	protected String getDependencyNotation() {
		return "'dev.nokee.test:${libraryProjectName}:1.0'"
	}

	protected String configureLibraryProject() {
		return """
			group = 'dev.nokee.test'
			version = '1.0'
		"""
	}

	protected String configureMultiProjectBuild() {
		return """
			includeBuild '${libraryProjectName}'
		"""
	}
}
