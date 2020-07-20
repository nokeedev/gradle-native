package dev.nokee.ide.xcode

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.ide.xcode.fixtures.XcodeIdeProjectFixture
import dev.nokee.ide.xcode.fixtures.XcodeIdeWorkspaceFixture
import dev.nokee.platform.nativebase.internal.OperatingSystemOperations
import org.apache.commons.lang3.SystemUtils
import spock.lang.Requires

abstract class AbstractXcodeIdeNativeComponentPluginFunctionalTest extends AbstractXcodeIdeFunctionalSpec {
	protected abstract void makeSingleProject();

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

	protected List<String> getComponentUnderTestSourceLayout() {
		def className = this.class.simpleName

		def groupNamePrefix = projectName
		if (className.startsWith('XcodeIdeSwift')) {
			groupNamePrefix = groupNamePrefix.capitalize()
		}

		if (className.contains('WithBothLinkage')) {
			return componentUnderTest.files.collect { file ->
				return ['Static', 'Shared'].collect { "${groupNamePrefix}${it}/${file.name}".toString() }
			}.flatten()
		}
		return componentUnderTest.files.collect { "${groupNamePrefix}/${it.name}".toString() }
	}

	protected abstract String getProjectName();

	protected String getSchemeName() {
		return projectName
	}

	protected String getWorkspaceName() {
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
		return [":mainXcodeProject", ':xcodeWorkspace', ':xcode']
	}

	def "includes sources in the project"() {
		settingsFile << "rootProject.name = '${projectName}'"
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('xcode')

		then:
		xcodeProjectUnderTest.assertHasSourceLayout(componentUnderTestSourceLayout + productNames.collect { "Products/$it".toString() } + ['build.gradle', 'settings.gradle'])
	}

	def "include sources in project with custom layout"() {
		settingsFile << "rootProject.name = '${projectName}'"
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
		xcodeProjectUnderTest.assertHasSourceLayout(componentUnderTestSourceLayout + productNames.collect { "Products/$it".toString() } + ['build.gradle', 'settings.gradle'])
	}

	@Requires({ SystemUtils.IS_OS_MAC })
	def "can build from Xcode IDE"() {
		useXcodebuildTool()
		settingsFile << configurePluginClasspathAsBuildScriptDependencies() << """
			rootProject.name = '${projectName}'
		"""
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('xcode')

		then:
		result.assertTasksExecuted(allTasksToXcode)

		and:
		def result = xcodebuild.withWorkspace(xcodeWorkspaceUnderTest).withScheme(schemeName).succeeds()
		result.assertTasksExecuted(allTasksForBuildAction, ":_xcode___${projectName}_${schemeName}_Default")
	}

	@Requires({ SystemUtils.IS_OS_MAC })
	def "can build from Xcode IDE without source"() {
		useXcodebuildTool()
		settingsFile << configurePluginClasspathAsBuildScriptDependencies() << """
			rootProject.name = '${projectName}'
		"""
		makeSingleProject()

		when:
		succeeds('xcode')

		then:
		result.assertTasksExecuted(allTasksToXcode)

		and:
		def result = xcodebuild.withWorkspace(xcodeWorkspaceUnderTest).withScheme(schemeName).succeeds()
		// TODO: Bridge task should be skipped with no source
		result.assertTasksExecuted(allTasksForBuildAction, ":_xcode___${projectName}_${schemeName}_Default")
	}

	@Requires({ SystemUtils.IS_OS_MAC })
	def "can clean relocated xcode derived data relative to workspace"() {
		useXcodebuildTool()
		settingsFile << configurePluginClasspathAsBuildScriptDependencies() << """
			rootProject.name = '${projectName}'
		"""
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

		when: 'clean keeps Xcode derived data'
		succeeds('clean')
		then:
		file('.gradle/XcodeDerivedData').assertIsDirectory()

		when: 'clean Xcode deletes derived data'
		succeeds('cleanXcode')
		then:
		file('.gradle/XcodeDerivedData').assertDoesNotExist()
		file('.gradle').assertExists()
	}
}
