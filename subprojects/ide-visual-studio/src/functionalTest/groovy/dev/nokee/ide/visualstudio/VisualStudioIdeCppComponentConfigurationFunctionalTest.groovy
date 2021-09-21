/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.ide.visualstudio

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.language.cpp.tasks.CppCompile
import dev.nokee.platform.nativebase.NativeBinary
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp
import dev.nokee.platform.nativebase.fixtures.CppGreeterLib
import spock.lang.Unroll

abstract class AbstractVisualStudioIdeCppComponentConfigurationFunctionalTest extends AbstractGradleSpecification implements VisualStudioIdeFixture, CppTaskNames {
	@Unroll
	def "configures C++ language support in generated projects"(compilerFlag, expectedLanguageStandard, uniqueIndex) {
		given:
		settingsFile << "rootProject.name = 'root'"
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << """
			import ${NativeBinary.canonicalName}
			import ${CppCompile.canonicalName}
			${componentUnderTestDsl} {
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
		visualStudioProject('root').projectConfigurations*.languageStandard.every { it == expectedLanguageStandard }

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

	protected abstract SourceElement getComponentUnderTest()

	protected abstract String getComponentUnderTestDsl()

	protected abstract void makeSingleProject()
}

class VisualStudioIdeCppApplicationConfigurationFunctionalTest extends AbstractVisualStudioIdeCppComponentConfigurationFunctionalTest {
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
	protected SourceElement getComponentUnderTest() {
		return new CppGreeterApp()
	}

	@Override
	protected String getComponentUnderTestDsl() {
		return 'application'
	}
}

class VisualStudioIdeCppLibraryConfigurationFunctionalTest extends AbstractVisualStudioIdeCppComponentConfigurationFunctionalTest {
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
	protected SourceElement getComponentUnderTest() {
		return new CppGreeterLib()
	}

	@Override
	protected String getComponentUnderTestDsl() {
		return 'library'
	}
}

class VisualStudioIdeCppLibraryWithStaticLinkageConfigurationFunctionalTest extends VisualStudioIdeCppLibraryConfigurationFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << """
			library {
				targetLinkages = [linkages.static]
			}
		"""
	}
}

class VisualStudioIdeCppLibraryWithSharedLinkageConfigurationFunctionalTest extends VisualStudioIdeCppLibraryConfigurationFunctionalTest {
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

class VisualStudioIdeCppLibraryWithBothLinkageConfigurationFunctionalTest extends VisualStudioIdeCppLibraryConfigurationFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << """
			library {
				targetLinkages = [linkages.static, linkages.shared]
			}
		"""
	}
}
