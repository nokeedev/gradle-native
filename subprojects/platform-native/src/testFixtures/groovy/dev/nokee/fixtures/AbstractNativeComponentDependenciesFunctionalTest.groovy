package dev.nokee.fixtures

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec

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
