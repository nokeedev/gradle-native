package dev.nokee.ide.fixtures

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.gradleplugins.fixtures.sources.SourceElement

import static org.junit.Assume.assumeFalse

abstract class AbstractIdeNativeComponentPluginFunctionalTest extends AbstractGradleSpecification {
	def "generates IDE files with build type awareness"() {
		assumeFalse(this.class.simpleName in ['VisualStudioIdeCApplicationWithNativeTestSuiteFunctionalTest', 'VisualStudioIdeCLibraryWithNativeTestSuiteFunctionalTest', 'VisualStudioIdeCppApplicationWithNativeTestSuiteFunctionalTest', 'VisualStudioIdeCppLibraryWithNativeTestSuiteFunctionalTest', 'XcodeIdeSwiftLibraryWithBothLinkageFunctionalTest', 'XcodeIdeSwiftLibraryWithNativeTestSuiteFunctionalTest'])
		given:
		settingsFile << configureProjectName()
		makeSingleProject()
		buildFile << configureBuildTypes('debug', 'release')
		componentUnderTest.writeToProject(testDirectory)

		when:
		def result = succeeds(ideTasks.ideLifecycle)

		then:
		result.assertTasksExecutedAndNotSkipped(allTasksToIde('debug', 'release'))
		ideProjectUnderTest.assertHasBuildTypes('debug', 'release')
	}

	protected abstract String configureProjectName()

	protected abstract IdeWorkspaceFixture getIdeWorkspaceUnderTest()

	protected abstract IdeProjectFixture getIdeProjectUnderTest()

	protected String getIdeComponentNameUnderTest() {
		return 'main'
	}

	protected abstract SourceElement getComponentUnderTest()

	protected abstract void makeSingleProject()

	protected String configureBuildTypes(String... buildTypes) {
		return """
			${componentUnderTestDsl} {
				targetBuildTypes = [${buildTypes.collect { "buildTypes.named('${it}')" }.join(', ')}]
			}
		"""
	}

	protected abstract String getComponentUnderTestDsl()

	protected abstract IdeWorkspaceTasks getIdeTasks()

	protected List<String> allTasksToIde(String... buildTypes) {
		return ideTasks.allToIde(ideComponentNameUnderTest)
	}
}
