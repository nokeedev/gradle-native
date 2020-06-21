package dev.nokee.fixtures

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily

abstract class AbstractNativeComponentDependenciesFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	protected String getLibraryProjectName() {
		return 'library'
	}

	def "can define implementation dependencies on component"() {
		given:
		makeComponentWithLibrary()
		buildFile << """
			${componentUnderTestDsl} {
				dependencies {
					implementation ${dependencyNotation}
				}
			}
		"""

		when:
		run(tasks.assemble)

		then:
		result.assertTasksExecuted(libraryTasks, tasks.allToAssemble)
	}

	def "can define implementation dependencies on each variant"() {
		given:
		makeComponentWithLibrary()
		buildFile << """
			${componentUnderTestDsl} {
				variants.configureEach { b ->
					dependencies {
						implementation ${dependencyNotation}
					}
				}
			}
		"""

		when:
		run(tasks.assemble)

		then:
		result.assertTasksExecuted(libraryTasks, tasks.allToAssemble)
	}

	// TODO: Variant-aware configuration
	def "can define implementation dependencies single variant"() {
		given:
		makeComponentWithLibrary()
		buildFile << """
			import ${DefaultOperatingSystemFamily.canonicalName}

			${componentUnderTestDsl} {
				targetMachines = [machines.macOS, machines.windows]
				variants.configureEach({it.buildVariant.getAxisValue(${DefaultOperatingSystemFamily.simpleName}.DIMENSION_TYPE).macOS}) {
					dependencies {
						implementation ${dependencyNotation}
					}
				}
			}
		"""

		when:
		run(tasks.withOperatingSystemFamily('macos').assemble, '--dry-run')
		then:
		// TODO: https://github.com/gradle-plugins/toolbox/issues/15
		(libraryTasks + tasks.withOperatingSystemFamily('macos').allToAssemble).each { result.assertOutputContains(it) }

		when:
		run(tasks.withOperatingSystemFamily('windows').assemble, '--dry-run')
		then:
		// TODO: https://github.com/gradle-plugins/toolbox/issues/15
		tasks.withOperatingSystemFamily('windows').allToAssemble.each { result.assertOutputContains(it) }
	}

	// TODO: Add test for source dependencies

//	def "can define different implementation dependencies on each variant"() {
//		given:
//		makeComponentWithLibrary()
//		buildFile << """
//            ${componentUnderTestDsl} {
//                binaries.getByName('mainDebug').configure {
//                    dependencies {
//                        implementation project(':${libraryProjectName}')
//                    }
//                }
//            }
//        """
//
//		when:
//		run(':assembleDebug')
//
//		then:
//		result.assertTasksExecuted(libDebugTasks, assembleDebugTasks, ':assembleDebug')
//
//		when:
//		run(':assembleRelease')
//
//		then:
//		result.assertTasksExecuted(assembleReleaseTasks, ':assembleRelease')
//	}

	/**
	 * Creates a build with the component under test in the root project and a library in the 'lib' project.
	 */
	protected abstract void makeComponentWithLibrary()

	protected abstract String getComponentUnderTestDsl()

	protected abstract List<String> getLibraryTasks()

	protected abstract String getDependencyNotation()
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
