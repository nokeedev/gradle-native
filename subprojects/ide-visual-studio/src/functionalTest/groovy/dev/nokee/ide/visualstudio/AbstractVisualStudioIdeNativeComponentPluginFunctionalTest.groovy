package dev.nokee.ide.visualstudio

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeProjectFixture
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeSolutionFixture
import org.apache.commons.lang3.SystemUtils
import spock.lang.Requires

abstract class AbstractVisualStudioIdeNativeComponentPluginFunctionalTest extends AbstractGradleSpecification implements VisualStudioIdeFixture {
	protected String configureProjectName() {
		return """
			rootProject.name = '${solutionName}'
		"""
	}

	protected abstract void makeSingleProject()

	protected abstract SourceElement getComponentUnderTest()

	protected abstract String configureCustomSourceLayout()

	protected abstract String getVisualStudioProjectName()

	protected String getSolutionName() {
		String solutionName = 'app'
		if (this.class.simpleName.contains('Library')) {
			solutionName = 'lib'
		}
		return solutionName
	}

	protected VisualStudioIdeProjectFixture getVisualStudioProjectUnderTest() {
		return visualStudioProject(visualStudioProjectName)
	}

	protected VisualStudioIdeSolutionFixture getVisualStudioSolutionUnderTest() {
		return visualStudioSolution(solutionName)
	}

	protected abstract List<String> getAllTasksForBuildAction()

	protected List<String> getAllTasksToXcode() {
		return [":${visualStudioProjectName}VisualStudioProject", ':visualStudioSolution', ':visualStudio']
	}

	protected List<String> getExpectedProjectNames() {
		def className = this.class.simpleName
		def result = []
		if (className.contains('Application')) {
			result.add('app')
		} else if (className.contains('Library')) {
			result.add('lib')
		} else {
			throw new UnsupportedOperationException()
		}

		if (className.contains('WithNativeTestSuite')) {
			result.addAll(result.collect { "${it}-test" })
		}
		return result
	}

	protected String getVisualStudioIdeBridge() {
		return ":_visualStudio__build_${visualStudioProjectName.toLowerCase()}_Default_x64"
	}

	def "creates Visual Studio project delegating to Gradle"() {
		given:
		settingsFile << configureProjectName()
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('visualStudio')

		then:
		def project = visualStudioProjectUnderTest
		project.assertHasTarget('Build')
		project.getTargetByName('Build').assertTargetDelegateToGradle()

		and:
		project.assertHasTarget('Clean')
		project.getTargetByName('Clean').assertTargetDelegateToGradle()
	}

	def "includes sources in the project"() {
		given:
		settingsFile << configureProjectName()
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('visualStudio')

		then:
		visualStudioProjectUnderTest.assertHasSourceLayout(componentUnderTest.sources.files.collect { "Source Files/${it.name}".toString() } + componentUnderTest.headers.files.collect { "Header Files/${it.name}".toString() } + ['build.gradle', 'settings.gradle'])
	}

	def "include sources in project with custom layout"() {
		given:
		settingsFile << configureProjectName()
		makeSingleProject()
		componentUnderTest.sources.writeToSourceDir(file('srcs'))
		componentUnderTest.headers.writeToSourceDir(file('hdrs'))
		buildFile << configureCustomSourceLayout()

		when:
		succeeds('visualStudio')

		then:
		visualStudioProjectUnderTest.assertHasSourceLayout(componentUnderTest.sources.files.collect { "Source Files/${it.name}".toString() } + componentUnderTest.headers.files.collect { "Header Files/${it.name}".toString() } + ['build.gradle', 'settings.gradle'])
	}

	def "all projects appears in solution"() {
		given:
		settingsFile << configureProjectName()
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('visualStudio')

		then:
		visualStudioSolutionUnderTest.assertHasProjects(expectedProjectNames)
	}

	@Requires({SystemUtils.IS_OS_WINDOWS})
	def "build generated visual studio solution"() {
		using msbuildTool

		given:
		settingsFile << configurePluginClasspathAsBuildScriptDependencies() << configureProjectName()
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		run "visualStudio"

		when:
		def result = msbuild
			.withWorkingDirectory(testDirectory)
			.withSolution(visualStudioSolutionUnderTest)
			.withConfiguration("Default")
			.withProject(visualStudioProjectName)
			.succeeds()

		then:
		result.assertTasksExecuted(allTasksForBuildAction, visualStudioIdeBridge)
//		file(getBuildFile(VariantContext.of(buildType: 'debug', architecture: 'x86'))).assertIsFile()
	}

	// TODO: Check ConfigurationType
	// TODO: Configure language standard
	// TODO: Execute solution with MSBuild
	// TODO: Test cleanVisualStudio
	// TODO: Build component copy the product in the right location
	// TODO: For library with both linkage, we can change the configuration type to static/shared and build change accordingly
	// TODO: For library with both linkage, default to configuration type of DynamicLibrary (what the development variant points to)

//	def "can generate Visual Studio IDE files"() {
//		println testDirectory
//		settingsFile << """
//			rootProject.name = '${projectName}'
//		"""
//		makeSingleProject()
//		componentUnderTest.writeToProject(testDirectory)
//
//		when:
//		succeeds('visualStudio')
//
//		then:
//		result.assertTasksExecuted(allTasksToXcode)
//	}
//
//	def "can generate multiple Visual Studio IDE files"() {
//		println testDirectory
//		settingsFile << """
//			rootProject.name = '${projectName}'
//			include 'library'
//		"""
//		makeSingleProject()
//		buildFile << '''
//			application {
//				dependencies {
//					implementation project(':library')
//				}
//			}
//		'''
//		file('library', buildFileName) << '''
//			plugins {
//				id 'dev.nokee.cpp-library'
//				id 'dev.nokee.visual-studio-ide'
//			}
//		'''
//		new CppMainUsesGreeter().writeToProject(testDirectory)
//		new CppGreeter().asLib().writeToProject(testDirectory.file('library'))
//
//		when:
//		succeeds(':visualStudio')
//
//		then:
//		result.assertTasksExecuted(':appVisualStudioProject', ':library:libraryVisualStudioProject', ':visualStudioSolution', ':visualStudio')
//	}
//
//	@Ignore
//	def "can call from ide"() {
//		println testDirectory
//		settingsFile << """
//			rootProject.name = '${projectName}'
//			include 'library'
//		"""
//		makeSingleProject()
//		buildFile << '''
//			application {
//				dependencies {
//					implementation project(':library')
//				}
//			}
//		'''
//		file('library', buildFileName) << '''
//			plugins {
//				id 'dev.nokee.cpp-library'
//				id 'dev.nokee.visual-studio-ide'
//			}
//		'''
//		new CppMainUsesGreeter().writeToProject(testDirectory)
//		new CppGreeter().asLib().writeToProject(testDirectory.file('library'))
//
//		when:
//		succeeds(':_visualStudio__build_app_Default_x64')
//
//		then:
//		result.assertTasksExecuted(':appVisualStudioProject', ':library:libraryVisualStudioProject', ':visualStudioSolution', ':visualStudio')
//	}

//
//	@Requires({ SystemUtils.IS_OS_MAC })
//	def "can build from Xcode IDE"() {
//		useXcodebuildTool()
//		settingsFile << configurePluginClasspathAsBuildScriptDependencies() << """
//			rootProject.name = '${projectName}'
//		"""
//		makeSingleProject()
//		componentUnderTest.writeToProject(testDirectory)
//
//		when:
//		succeeds('xcode')
//
//		then:
//		result.assertTasksExecuted(allTasksToXcode)
//
//		and:
//		def result = xcodebuild.withWorkspace(xcodeWorkspace(workspaceName)).withScheme(schemeName).succeeds()
//		result.assertTasksExecuted(allTasksForBuildAction, ":_xcode___${projectName}_${schemeName}_Default")
//	}
//
//	@Requires({ SystemUtils.IS_OS_MAC })
//	def "can clean relocated xcode derived data relative to workspace"() {
//		useXcodebuildTool()
//		settingsFile << configurePluginClasspathAsBuildScriptDependencies() << """
//			rootProject.name = '${projectName}'
//		"""
//		makeSingleProject()
//		componentUnderTest.writeToProject(testDirectory)
//
//		when: 'can generate relocated Xcode derived data workspace'
//		succeeds('xcode')
//		then:
//		result.assertTasksExecuted(allTasksToXcode)
//		and:
//		xcodeWorkspace(workspaceName).assertDerivedDataLocationRelativeToWorkspace('.gradle/XcodeDerivedData')
//		and:
//		file('.gradle/XcodeDerivedData').assertDoesNotExist()
//
//		when: 'Xcode build inside relocated derived data'
//		xcodebuild.withWorkspace(xcodeWorkspace(workspaceName)).withScheme(schemeName).succeeds()
//		then:
//		file('.gradle/XcodeDerivedData').assertIsDirectory()
//
//		when: 'clean keeps Xcode derived data'
//		succeeds('clean')
//		then:
//		file('.gradle/XcodeDerivedData').assertIsDirectory()
//
//		when: 'clean Xcode deletes derived data'
//		succeeds('cleanXcode')
//		then:
//		file('.gradle/XcodeDerivedData').assertDoesNotExist()
//		file('.gradle').assertExists()
//	}
}
