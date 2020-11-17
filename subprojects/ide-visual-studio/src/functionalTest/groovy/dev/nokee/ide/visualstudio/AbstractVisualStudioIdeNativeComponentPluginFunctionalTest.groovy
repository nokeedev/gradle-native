package dev.nokee.ide.visualstudio

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.ide.fixtures.AbstractIdeNativeComponentPluginFunctionalTest
import dev.nokee.ide.fixtures.IdeProjectFixture
import dev.nokee.ide.fixtures.IdeWorkspaceFixture
import dev.nokee.ide.fixtures.IdeWorkspaceTasks
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeProjectFixture
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeSolutionFixture
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeTaskNames
import org.apache.commons.lang3.SystemUtils
import spock.lang.Ignore
import spock.lang.Requires

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofHeaders
import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofSources
import static org.junit.Assume.assumeFalse

abstract class AbstractVisualStudioIdeNativeComponentPluginFunctionalTest extends AbstractIdeNativeComponentPluginFunctionalTest implements VisualStudioIdeFixture {
	protected String configureProjectName() {
		return """
			rootProject.name = '${visualStudioSolutionName}'
		"""
	}

	protected abstract void makeSingleProject()

	protected abstract SourceElement getComponentUnderTest()

	protected abstract String configureCustomSourceLayout()

	protected String configureBuildTypes(String... buildTypes) {
		return """
			${componentUnderTestDsl} {
				targetBuildTypes = [${buildTypes.collect { "buildTypes.named('${it}')"}.join(', ')}]
			}
		"""
	}

	protected String configurePlatforms(String... platforms) {
		return """
			${componentUnderTestDsl} {
				targetMachines = [${platforms.join(', ')}]
			}
		"""
	}

	protected abstract String getVisualStudioProjectName()

	protected String getVisualStudioSolutionName() {
		return gradleProjectName
	}

	protected String getGradleProjectName() {
		String projectName = 'app'
		if (this.class.simpleName.contains('Library')) {
			projectName = 'lib'
		}
		return projectName
	}

	protected List<String> getGeneratedHeaders() {
		return []
	}

	protected VisualStudioIdeProjectFixture getVisualStudioProjectUnderTest() {
		return visualStudioProject(visualStudioProjectName)
	}

	protected VisualStudioIdeSolutionFixture getVisualStudioSolutionUnderTest() {
		return visualStudioSolution(visualStudioSolutionName)
	}

	protected final List<String> getAllTasksForBuildAction() {
		return allTasksForBuildAction('')
	}

	protected abstract List<String> allTasksForBuildAction(String variant)

	protected List<String> getAllTasksToXcode() {
		return [":mainVisualStudioProject", ':visualStudioSolution', ':visualStudio']
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

	protected String getComponentUnderTestDsl() {
		def className = this.class.simpleName
		if (className.contains('WithNativeTestSuite')) {
			return 'testSuites.configureEach'
		} else if (className.contains('Application')) {
			return 'application'
		} else if (className.contains('Library')) {
			return 'library'
		}
		throw new UnsupportedOperationException()
	}

	protected String getVisualStudioIdeBridge() {
		return ":_visualStudio__build_${visualStudioProjectName.toLowerCase()}_default_x64"
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
		visualStudioProjectUnderTest.assertHasSourceLayout(ofSources(componentUnderTest).files.collect { "Source Files/${it.name}".toString() } + ofHeaders(componentUnderTest).files.collect { "Header Files/${it.name}".toString() } + generatedHeaders.collect { "Header Files/${it}".toString() } + ['build.gradle', 'settings.gradle'])
	}

	def "include sources in project with custom layout"() {
		given:
		settingsFile << configureProjectName()
		makeSingleProject()
		ofSources(componentUnderTest).writeToSourceDir(file('srcs'))
		ofHeaders(componentUnderTest).writeToSourceDir(file('hdrs'))
		buildFile << configureCustomSourceLayout()

		when:
		succeeds('visualStudio')

		then:
		visualStudioProjectUnderTest.assertHasSourceLayout(ofSources(componentUnderTest).files.collect { "Source Files/${it.name}".toString() } + ofHeaders(componentUnderTest).files.collect { "Header Files/${it.name}".toString() } + ['build.gradle', 'settings.gradle'])
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

	@Ignore("because left over Gradle daemons cause failures")
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
			.withConfiguration("default")
			.withProject(visualStudioProjectName)
			.succeeds()

		then:
		result.assertTasksExecuted(allTasksForBuildAction, visualStudioIdeBridge)
//		file(getBuildFile(VariantContext.of(buildType: 'debug', architecture: 'x86'))).assertIsFile()
	}

	def "can generate projects and solution for multiple build types"() {
		assumeFalse(this.class.simpleName.contains('WithNativeTestSuite'))
		given:
		settingsFile << configureProjectName()
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << configureBuildTypes('debug', 'release')

		when:
		succeeds('visualStudio')

		then:
		visualStudioSolutionUnderTest.assertHasProjectConfigurations('debug|x64', 'release|x64')
		visualStudioProjectUnderTest.assertHasProjectConfigurations('debug|x64', 'release|x64')
	}

	def "can generate projects and solution for multiple machine architecture"() {
		assumeFalse(this.class.simpleName.contains('WithNativeTestSuite'))
		given:
		settingsFile << configureProjectName()
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << configurePlatforms('machines.windows.x86', 'machines.windows.x86_64')

		when:
		succeeds('visualStudio')

		then:
		visualStudioSolutionUnderTest.assertHasProjectConfigurations('default|Win32', 'default|x64')
		visualStudioProjectUnderTest.assertHasProjectConfigurations('default|Win32', 'default|x64')
	}

	def "can build different machine architecture for projects [staged]"() {
		assumeFalse(this.class.simpleName.contains('WithNativeTestSuite'))
		given:
		settingsFile << configureProjectName()
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << configurePlatforms('machines.windows.x86', 'machines.windows.x86_64')

		when:
		def resultX86 = succeeds('-Pdev.nokee.internal.visualStudio.bridge.Action=build', "-Pdev.nokee.internal.visualStudio.bridge.OutDir=.vs/x86", "-Pdev.nokee.internal.visualStudio.bridge.PlatformName=Win32", "-Pdev.nokee.internal.visualStudio.bridge.Configuration=default", "-Pdev.nokee.internal.visualStudio.bridge.ProjectName=${gradleProjectName}", "-Pdev.nokee.internal.visualStudio.bridge.GRADLE_IDE_PROJECT_NAME=main", ":_visualStudio__build_${gradleProjectName}_default_Win32", "--dry-run")
		then:
		resultX86.assertTasksExecuted(allTasksForBuildAction('x86'), ":_visualStudio__build_${gradleProjectName}_default_Win32")

		when:
		def resultX64 = succeeds('-Pdev.nokee.internal.visualStudio.bridge.Action=build', "-Pdev.nokee.internal.visualStudio.bridge.OutDir=.vs/x64", "-Pdev.nokee.internal.visualStudio.bridge.PlatformName=x64", "-Pdev.nokee.internal.visualStudio.bridge.Configuration=default", "-Pdev.nokee.internal.visualStudio.bridge.ProjectName=${gradleProjectName}", "-Pdev.nokee.internal.visualStudio.bridge.GRADLE_IDE_PROJECT_NAME=main", ":_visualStudio__build_${gradleProjectName}_default_x64", "--dry-run")
		then:
		resultX64.assertTasksExecuted(allTasksForBuildAction('x86-64'), ":_visualStudio__build_${gradleProjectName}_default_x64")
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
	@Override
	protected IdeWorkspaceFixture getIdeWorkspaceUnderTest() {
		return visualStudioSolutionUnderTest
	}

	@Override
	protected IdeProjectFixture getIdeProjectUnderTest() {
		return visualStudioProjectUnderTest
	}

	@Override
	protected IdeWorkspaceTasks getIdeTasks() {
		return new VisualStudioIdeTaskNames.VisualStudioIdeSolutionTasks()
	}
}
