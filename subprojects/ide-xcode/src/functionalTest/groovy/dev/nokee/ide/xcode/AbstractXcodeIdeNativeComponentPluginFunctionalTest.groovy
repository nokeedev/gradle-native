package dev.nokee.ide.xcode

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.ide.xcode.fixtures.XcodeIdeProjectFixture
import dev.nokee.ide.xcode.fixtures.XcodeIdeWorkspaceFixture
import dev.nokee.platform.nativebase.internal.OperatingSystemOperations
import org.apache.commons.lang3.SystemUtils
import spock.lang.Requires

abstract class AbstractXcodeIdeNativeComponentPluginFunctionalTest extends AbstractGradleSpecification implements XcodeIdeFixture {
	protected String configureProjectName() {
		String projectName = 'app'
		if (this.class.simpleName.contains('Library')) {
			projectName = 'lib'
		}
		return """
			rootProject.name = '${projectName}'
		"""
	}

	protected abstract void makeSingleProject();

	protected void makeSingleProjectWithoutSources() {
		makeSingleProject()
	}

	protected abstract SourceElement getComponentUnderTest();

	protected String configureCustomSourceLayout() {
		def componentUnderTestDsl = 'application'
		if (this.class.simpleName.contains('Library')) {
			componentUnderTestDsl = 'library'
		}

		def result = """
			${componentUnderTestDsl} {
				sources.from('srcs')
			}
		"""

		if (!this.class.simpleName.startsWith("XcodeIdeSwift")) {
			result = result + """
				${componentUnderTestDsl} {
					privateHeaders.from('hdrs')
				}
			"""
		}

		return result
	}

	protected List<String> getProductNames() {
		def className = this.class.simpleName

		def productName = projectName
		if (className.startsWith('XcodeIdeSwift')) {
			productName = productName.capitalize()
		}

		def osOperations = OperatingSystemOperations.ofHost()
		if (className.contains('Application')) {
			return [osOperations.getExecutableName(productName)]
		} else if (className.contains('Library')) {
			if (className.contains('WithStaticLinkage')) {
				return [osOperations.getStaticLibraryName(productName)]
			} else if (className.contains('WithBothLinkage')) {
				return [osOperations.getSharedLibraryName(productName), osOperations.getStaticLibraryName(productName)]
			}
			return [osOperations.getSharedLibraryName(productName)]
		}
		throw new IllegalArgumentException()
	}

	protected abstract String getProjectName();

	protected String getSchemeName() {
		return groupName
	}

	protected String getWorkspaceName() {
		return projectName
	}

	protected String getGroupName() {
		return projectName
	}

	protected XcodeIdeProjectFixture getXcodeProjectUnderTest() {
		return xcodeProject(projectName)
	}

	protected XcodeIdeWorkspaceFixture getXcodeWorkspaceUnderTest() {
		return xcodeWorkspace(workspaceName)
	}

	protected abstract List<String> getAllTasksForBuildAction()

	protected List<String> getAllTasksToXcode() {
		return [":${projectName.toLowerCase()}XcodeProject", ':xcodeWorkspace', ':xcode']
	}

	protected String getXcodeIdeBridge() {
		return ":_xcode___${projectName.toLowerCase()}_${schemeName}_Default"
	}

	def "includes sources in the project"() {
		given:
		settingsFile << configureProjectName()
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('xcode')

		then:
		xcodeProjectUnderTest.getGroupByName(groupName).assertHasSourceLayout(componentUnderTest.files*.name as List)
		xcodeProjectUnderTest.productsGroup.files.containsAll(productNames)
		xcodeProjectUnderTest.mainGroup.files == ['build.gradle', 'settings.gradle'] as Set
	}

	def "include sources in project with custom layout"() {
		settingsFile << configureProjectName()
		makeSingleProject()
		if (this.class.simpleName.startsWith('XcodeIdeSwift')) {
			componentUnderTest.writeToSourceDir(file('srcs'))
		} else {
			componentUnderTest.sources.writeToSourceDir(file('srcs'))
			componentUnderTest.headers.writeToSourceDir(file('hdrs'))
		}
		buildFile << configureCustomSourceLayout()

		when:
		succeeds('xcode')

		then:
		xcodeProjectUnderTest.getGroupByName(groupName).assertHasSourceLayout(componentUnderTest.files*.name as List)
		xcodeProjectUnderTest.productsGroup.files.containsAll(productNames)
		xcodeProjectUnderTest.mainGroup.files == ['build.gradle', 'settings.gradle'] as Set
	}

	@Requires({ SystemUtils.IS_OS_MAC })
	def "can build from Xcode IDE"() {
		using xcodebuildTool
		settingsFile << configurePluginClasspathAsBuildScriptDependencies() << configureProjectName()
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('xcode')

		then:
		result.assertTasksExecuted(allTasksToXcode)

		and:
		def result = xcodebuild.withWorkspace(xcodeWorkspaceUnderTest).withScheme(schemeName).succeeds()
		result.assertTasksExecuted(allTasksForBuildAction, xcodeIdeBridge)
	}

	@Requires({ SystemUtils.IS_OS_MAC })
	def "can build from Xcode IDE without source"() {
		using xcodebuildTool
		settingsFile << configurePluginClasspathAsBuildScriptDependencies() << configureProjectName()
		makeSingleProjectWithoutSources()

		when:
		succeeds('xcode')

		then:
		result.assertTasksExecuted(allTasksToXcode)

		and:
		def result = xcodebuild.withWorkspace(xcodeWorkspaceUnderTest).withScheme(schemeName).succeeds()
		// TODO: Bridge task should be skipped with no source
		result.assertTasksExecuted(allTasksForBuildAction, xcodeIdeBridge)
	}

	@Requires({ SystemUtils.IS_OS_MAC })
	def "relocates xcode derived data relative to workspace"() {
		using xcodebuildTool
		settingsFile << configurePluginClasspathAsBuildScriptDependencies() << configureProjectName()
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when: 'can generate relocated Xcode derived data workspace'
		succeeds('xcode')
		then:
		result.assertTasksExecuted(allTasksToXcode)
		and:
		xcodeWorkspace(workspaceName).assertDerivedDataLocationRelativeToWorkspace('.gradle/XcodeDerivedData')
		and:
		file('.gradle/XcodeDerivedData').assertDoesNotExist()

		when: 'Xcode build inside relocated derived data'
		xcodebuild.withWorkspace(xcodeWorkspace(workspaceName)).withScheme(schemeName).succeeds()
		then:
		file('.gradle/XcodeDerivedData').assertIsDirectory()
	}
}
