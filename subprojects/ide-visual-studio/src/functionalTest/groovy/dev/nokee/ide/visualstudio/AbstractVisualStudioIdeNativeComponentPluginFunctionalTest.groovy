package dev.nokee.ide.visualstudio


import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.elements.CppGreeter
import dev.nokee.platform.nativebase.fixtures.CppMainUsesGreeter
import spock.lang.Ignore

abstract class AbstractVisualStudioIdeNativeComponentPluginFunctionalTest extends AbstractVisualStudioIdeFunctionalSpec {
	protected abstract void makeSingleProject();

	protected abstract SourceElement getComponentUnderTest();

	protected abstract String getProjectName();

	protected String getSchemeName() {
		return projectName
	}

	protected String getSolutionName() {
		return projectName
	}

	protected abstract List<String> getAllTasksForBuildAction()

	protected List<String> getAllTasksToXcode() {
		return [":${projectName}VisualStudioProject", ':visualStudioSolution', ':visualStudio']
	}

	def "creates Visual Studio project delegating to Gradle"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('visualStudio')

		then:
		def project = visualStudioProject
		project.assertHasTarget('Build')
		project.getTargetByName('Build').assertTargetDelegateToGradle()

		and:
		project.assertHasTarget('Clean')
		project.getTargetByName('Clean').assertTargetDelegateToGradle()
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
