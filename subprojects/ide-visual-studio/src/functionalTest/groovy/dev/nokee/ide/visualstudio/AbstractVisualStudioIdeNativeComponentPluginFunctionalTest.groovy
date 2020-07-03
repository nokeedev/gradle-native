package dev.nokee.ide.visualstudio

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.elements.CppGreeter
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp
import dev.nokee.platform.nativebase.fixtures.CppMainUsesGreeter

abstract class AbstractVisualStudioIdeNativeComponentPluginFunctionalTest extends AbstractGradleSpecification/*AbstractXcodeIdeFunctionalSpec*/ {
	protected abstract void makeSingleProject();

	protected abstract SourceElement getComponentUnderTest();

	protected abstract String getProjectName();

	protected String getSchemeName() {
		return projectName
	}

	protected String getWorkspaceName() {
		return projectName
	}

	protected abstract List<String> getAllTasksForBuildAction()

	protected List<String> getAllTasksToXcode() {
		return [":${projectName}VisualStudioProject", ':visualStudioSolution', ':visualStudio']
	}

	def "can generate Visual Studio IDE files"() {
		println testDirectory
		settingsFile << """
			rootProject.name = '${projectName}'
		"""
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('visualStudio')

		then:
		result.assertTasksExecuted(allTasksToXcode)
	}

	def "can generate multiple Visual Studio IDE files"() {
		println testDirectory
		settingsFile << """
			rootProject.name = '${projectName}'
			include 'library'
		"""
		makeSingleProject()
		buildFile << '''
			application {
				dependencies {
					implementation project(':library')
				}
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.cpp-library'
				id 'dev.nokee.visual-studio-ide'
			}
		'''
		new CppMainUsesGreeter().writeToProject(testDirectory)
		new CppGreeter().asLib().writeToProject(testDirectory.file('library'))

		when:
		succeeds(':visualStudio')

		then:
		result.assertTasksExecuted(':appVisualStudioProject', ':library:libraryVisualStudioProject', ':visualStudioSolution', ':visualStudio')
	}

	def "can call from ide"() {
		println testDirectory
		settingsFile << """
			rootProject.name = '${projectName}'
			include 'library'
		"""
		makeSingleProject()
		buildFile << '''
			application {
				dependencies {
					implementation project(':library')
				}
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.cpp-library'
				id 'dev.nokee.visual-studio-ide'
			}
		'''
		new CppMainUsesGreeter().writeToProject(testDirectory)
		new CppGreeter().asLib().writeToProject(testDirectory.file('library'))

		when:
		succeeds(':_visualStudio__build_app_Default_x64')

		then:
		result.assertTasksExecuted(':appVisualStudioProject', ':library:libraryVisualStudioProject', ':visualStudioSolution', ':visualStudio')
	}

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
