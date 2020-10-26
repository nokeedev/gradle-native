package dev.nokee.ide.xcode

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.ide.fixtures.AbstractIdeNativeComponentPluginFunctionalTest
import dev.nokee.ide.fixtures.IdeProjectFixture
import dev.nokee.ide.fixtures.IdeWorkspaceFixture
import dev.nokee.ide.fixtures.IdeWorkspaceTasks
import dev.nokee.ide.xcode.fixtures.XcodeIdeProjectFixture
import dev.nokee.ide.xcode.fixtures.XcodeIdeTaskNames
import dev.nokee.ide.xcode.fixtures.XcodeIdeWorkspaceFixture
import dev.nokee.platform.nativebase.internal.OperatingSystemOperations
import org.apache.commons.lang3.SystemUtils
import org.junit.Assume
import spock.lang.Requires

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofHeaders
import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofSources

abstract class AbstractXcodeIdeNativeComponentPluginFunctionalTest extends AbstractIdeNativeComponentPluginFunctionalTest implements XcodeIdeFixture {
	protected String configureProjectName() {
		return """
			rootProject.name = '${gradleProjectNameUnderTest}'
		"""
	}

	protected String getGradleProjectNameUnderTest() {
		if (this.class.simpleName.contains('Library')) {
			return 'lib'
		}
		return 'app'
	}

	protected abstract void makeSingleProject();

	protected void makeSingleProjectWithoutSources() {
		makeSingleProject()
	}

	protected abstract SourceElement getComponentUnderTest();

	protected String configureCustomSourceLayout() {
		def className = this.class.simpleName

		def componentUnderTestDsl = 'application'
		if (className.contains('Library')) {
			componentUnderTestDsl = 'library'
		}

		def languageName = ''
		if (className.contains('ObjectiveCpp')) {
			languageName = 'objectiveCpp'
		} else if (className.contains('ObjectiveC')) {
			languageName = 'objectiveC'
		} else if (className.contains('Cpp')) {
			languageName = 'cpp'
		} else if (className.contains('Swift')) {
			languageName = 'swift'
		} else if (className.contains('C')) {
			languageName = 'c'
		}


		def result = """
			${componentUnderTestDsl} {
				${languageName}Sources.from('srcs')
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

	protected String getComponentUnderTestDsl() {
		if (this.class.simpleName.contains('Application')) {
			return 'application'
		}
		return 'library'
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

	@Override
	protected String getIdeComponentNameUnderTest() {
		return gradleProjectNameUnderTest
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

	protected List<String> getAllTasksForBuildAction() {
		allTasksForBuildAction('')
	}

	protected List<String> allTasksForBuildAction(String buildType) {
		def className = this.class.simpleName
		if (className.contains('ApplicationWithNativeTestSuite')) {
			return [tasks.withBuildType(buildType).compile] + tasks.withComponentName('test').withBuildType(buildType).allToLink + [tasks.withComponentName('test').withBuildType(buildType).relocateMainSymbol]
		} else if (className.contains('Application')) {
			return tasks.withBuildType(buildType).allToLink
		} else if (className.contains('LibraryWithNativeTestSuite')) {
			return [tasks.withBuildType(buildType).compile] + tasks.withComponentName('test').withBuildType(buildType).allToLink
		} else if (className.contains('LibraryWithStaticLinkage')) {
			return tasks.forStaticLibrary.withBuildType(buildType).allToCreate
		} else if (className.contains('LibraryWithSharedLinkage')) {
			return tasks.withBuildType(buildType).allToLink
		} else if (className.contains('LibraryWithBothLinkage')) {
			return tasks.forSharedLibrary.withLinkage('shared').withBuildType(buildType).allToLink
		} else if (className.contains('Library')) {
			return tasks.withBuildType(buildType).allToLink
		}
		throw new UnsupportedOperationException()
	}

	protected List<String> getAllTasksToXcode() {
		return allTasksToIde()
	}

	protected List<String> allTasksToIde(String... buildTypes) {
		def result = [":${projectName.toLowerCase()}XcodeProject", ':xcodeWorkspace', ':xcode']

		def className = this.class.simpleName
		if (className.contains("Swift")) {
			def withBuildTypes = { tasks ->
				if (buildTypes.size() == 0) {
					return [tasks]
				}
				return buildTypes.collect { tasks.withBuildType(it) }
			}

			if (className.contains('ApplicationWithNativeTestSuite') || className.contains('LibraryWithNativeTestSuite')) {
				result += withBuildTypes(tasks).collect { it.compile }.flatten() + withBuildTypes(tasks.withComponentName('test')).collect { it.compile }.flatten()
			} else if (className.contains('Application')) {
				result += withBuildTypes(tasks).collect { it.compile }
			} else if (className.contains('LibraryWithStaticLinkage')) {
				result += withBuildTypes(tasks).collect { it.compile }
			} else if (className.contains('LibraryWithSharedLinkage')) {
				result += withBuildTypes(tasks).collect { it.compile }
			} else if (className.contains('LibraryWithBothLinkage')) {
				result += withBuildTypes(tasks.withLinkage('shared')).collect { it.compile } + withBuildTypes(tasks.withLinkage('static')).collect { it.compile }
			} else if (className.contains('Library')) {
				result += withBuildTypes(tasks).collect { it.compile }
			} else {
				throw new UnsupportedOperationException()
			}
		}
		return result
	}

	protected String getXcodeIdeBridge() {
		return xcodeIdeBridge('default')
	}

	protected String xcodeIdeBridge(String buildType) {
		return ":_xcode___${projectName.toLowerCase()}_${schemeName}_${buildType}"
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
			ofSources(componentUnderTest).writeToSourceDir(file('srcs'))
			ofHeaders(componentUnderTest).writeToSourceDir(file('hdrs'))
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

	// TODO: Generated project only has configuration debug / release changed

	@Requires({ SystemUtils.IS_OS_MAC })
	def "can build specific build types from IDE"() {
		// FIXME: Reenable this
		Assume.assumeFalse(this.class.simpleName.contains('SwiftLibraryWithNativeTestSuite'))
		Assume.assumeFalse(this.class.simpleName.contains('SwiftApplicationWithNativeTestSuite'))

		given:
		using xcodebuildTool
		settingsFile << configurePluginClasspathAsBuildScriptDependencies() << configureProjectName()
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureBuildTypes('debug', 'release')

		and:
		succeeds('xcode')
		result.assertTasksExecuted(allTasksToIde('debug', 'release'))

		when:
		def resultDebug = xcodebuild.withWorkspace(xcodeWorkspaceUnderTest).withScheme(schemeName).withConfiguration('debug').succeeds()
		then:
		resultDebug.assertTasksExecuted(allTasksForBuildAction('debug'), xcodeIdeBridge('debug'))

		when:
		def resultRelease = xcodebuild.withWorkspace(xcodeWorkspaceUnderTest).withScheme(schemeName).withConfiguration('release').succeeds()
		then:
		resultRelease.assertTasksExecuted(allTasksForBuildAction('release'), xcodeIdeBridge('release'))
	}

	@Override
	protected IdeWorkspaceFixture getIdeWorkspaceUnderTest() {
		return xcodeWorkspaceUnderTest
	}

	@Override
	protected IdeProjectFixture getIdeProjectUnderTest() {
		return xcodeProjectUnderTest
	}

	@Override
	protected IdeWorkspaceTasks getIdeTasks() {
		return new XcodeIdeTaskNames.XcodeIdeWorkspaceTasks()
	}
}
