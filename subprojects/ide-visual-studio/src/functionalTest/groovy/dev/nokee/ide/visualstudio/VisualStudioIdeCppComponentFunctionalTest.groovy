package dev.nokee.ide.visualstudio

import dev.gradleplugins.test.fixtures.sources.NativeSourceElement
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.language.cpp.tasks.CppCompile
import dev.nokee.platform.jni.fixtures.elements.CppGreeter
import dev.nokee.platform.nativebase.NativeBinary
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp
import spock.lang.Unroll

class VisualStudioIdeCppApplicationFunctionalTest extends AbstractVisualStudioIdeNativeComponentPluginFunctionalTest implements CppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-application'
				id 'dev.nokee.visual-studio-ide'
			}
		"""
	}

	@Override
	protected NativeSourceElement getComponentUnderTest() {
		return new CppGreeterApp()
	}

	@Override
	protected String configureCustomSourceLayout() {
		return '''
			application {
				sources.from('srcs')
				privateHeaders.from('hdrs')
			}
		'''
	}

	@Override
	protected String getProjectName() {
		return "app"
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.allToLink
	}

	@Unroll
	def "configures C++ language support in generated projects"(compilerFlag, expectedLanguageStandard, uniqueIndex) {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << """
			import ${NativeBinary.canonicalName}
			import ${CppCompile.canonicalName}
			application {
				binaries.configureEach(${NativeBinary.simpleName}) {
					compileTasks.configureEach(${CppCompile.simpleName}) {
						compilerArgs.add('${compilerFlag}')
					}
				}
			}
		"""

		when:
		succeeds('visualStudio')

		then:
		visualStudioProject.projectConfigurations*.languageStandard.every { it == expectedLanguageStandard }

		where:
		// UniqueIndex: // uniqueIndex: https://github.com/gradle/gradle/issues/8787
		compilerFlag     | expectedLanguageStandard | uniqueIndex
		''				 | 'Default'				| 0
		'/std:c++14'     | 'stdcpp14'               | 1
		'-std:c++14'     | 'stdcpp14'               | 2
		'/std:c++17'     | 'stdcpp17'               | 3
		'-std:c++17'     | 'stdcpp17'               | 4
		'/std:c++latest' | 'stdcpplatest'           | 5
		'-std:c++latest' | 'stdcpplatest'           | 6
	}
}

class VisualStudioIdeCppLibraryFunctionalTest extends AbstractVisualStudioIdeNativeComponentPluginFunctionalTest implements CppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-library'
				id 'dev.nokee.visual-studio-ide'
			}
		"""
	}

	@Override
	protected NativeSourceElement getComponentUnderTest() {
		return new CppGreeter().asLib()
	}

	@Override
	protected String configureCustomSourceLayout() {
		return '''
			library {
				sources.from('srcs')
				publicHeaders.from('hdrs')
			}
		'''
	}

	@Override
	protected String getProjectName() {
		return "lib"
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.allToLink
	}

	@Unroll
	def "configures C++ language support in generated projects"(compilerFlag, expectedLanguageStandard, uniqueIndex) {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << """
			import ${NativeBinary.canonicalName}
			import ${CppCompile.canonicalName}
			library {
				binaries.configureEach(${NativeBinary.simpleName}) {
					compileTasks.configureEach(${CppCompile.simpleName}) {
						compilerArgs.add('${compilerFlag}')
					}
				}
			}
		"""

		when:
		succeeds('visualStudio')

		then:
		visualStudioProject.projectConfigurations*.languageStandard.every { it == expectedLanguageStandard }

		where:
		// UniqueIndex: // uniqueIndex: https://github.com/gradle/gradle/issues/8787
		compilerFlag     | expectedLanguageStandard | uniqueIndex
		''				 | 'Default'				| 0
		'/std:c++14'     | 'stdcpp14'               | 1
		'-std:c++14'     | 'stdcpp14'               | 2
		'/std:c++17'     | 'stdcpp17'               | 3
		'-std:c++17'     | 'stdcpp17'               | 4
		'/std:c++latest' | 'stdcpplatest'           | 5
		'-std:c++latest' | 'stdcpplatest'           | 6
	}
}

class VisualStudioIdeCppLibraryWithStaticLinkageFunctionalTest extends VisualStudioIdeCppLibraryFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << """
			library {
				targetLinkages = [linkages.static]
			}
		"""
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.forStaticLibrary.allToCreate
	}
}

class VisualStudioIdeCppLibraryWithSharedLinkageFunctionalTest extends VisualStudioIdeCppLibraryFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << """
			library {
				targetLinkages = [linkages.shared]
			}
		"""
	}
}

class VisualStudioIdeCppLibraryWithBothLinkageFunctionalTest extends VisualStudioIdeCppLibraryFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << """
			library {
				targetLinkages = [linkages.static, linkages.shared]
			}
		"""
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.withLinkage('shared').allToLink
	}
}
