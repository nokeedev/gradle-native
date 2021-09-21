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
package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.integtests.fixtures.nativeplatform.AvailableToolChains
import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.JavaJniCGreeterLib
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib
import dev.nokee.platform.jni.fixtures.JavaJniObjectiveCGreeterLib
import dev.nokee.platform.jni.fixtures.JavaJniObjectiveCppGreeterLib
import spock.lang.Requires
import spock.util.environment.OperatingSystem

abstract class AbstractSharedLibraryConfigurationFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def "can define compiler flags from the model"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << """
			library.variants.configureEach {
				sharedLibrary.compileTasks.configureEach {
					compilerArgs.add('-DWITH_FEATURE')
				}
			}
		"""

		when:
		executer = executer.withArgument('-i')
		succeeds('assemble')

		then:
		outputContains('compiling with feature enabled')

		and:
		// TODO: Create the symetrie of linkerOptionsFor(...) inside AbstractInstalledToolChainIntegrationSpec
		file("build/tmp/${compileTaskName}/options.txt").text.contains('-DWITH_FEATURE')
	}

	protected abstract String getCompileTaskName()

	def "can define linker flags from the model"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << """
			library.variants.configureEach {
				sharedLibrary.linkTask.configure {
					linkerArgs.addAll(toolChain.map {
						if (it in VisualCpp) {
							return ['/Wall']
						}
						return ['-Werror']
					});
				}
			}
		"""

		when:
		executer = executer.withArgument('-i')
		succeeds('assemble')

		then:
		String expectedFlag = '-Werror'
		if (toolchainUnderTest.family == AvailableToolChains.ToolFamily.VISUAL_CPP) {
			expectedFlag = '/Wall'
		}
		linkerOptionsFor('link').options.text.contains(expectedFlag)
	}

	protected abstract void makeSingleProject();

	protected abstract SourceElement getComponentUnderTest();
}

class JavaCJniLibrarySharedLibraryConfigurationFunctionalTest extends AbstractSharedLibraryConfigurationFunctionalTest {
	@Override
	protected String getCompileTaskName() {
		return "compileC"
	}

	@Override
	protected void makeSingleProject() {
		settingsFile << "rootProject.name = 'jni-greeter'"
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.c-language'
			}
		'''
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniCGreeterLib('jni-greeter').withOptionalFeature()
	}
}

class JavaCppJniLibrarySharedLibraryConfigurationFunctionalTest extends AbstractSharedLibraryConfigurationFunctionalTest {
	@Override
	protected String getCompileTaskName() {
		return 'compileCpp'
	}

	@Override
	protected void makeSingleProject() {
		settingsFile << "rootProject.name = 'jni-greeter'"
		buildFile << '''
            plugins {
                id 'java'
                id 'dev.nokee.jni-library'
                id 'dev.nokee.cpp-language'
            }
        '''
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniCppGreeterLib('jni-greeter').withOptionalFeature()
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class JavaObjectiveCJniLibrarySharedLibraryConfigurationFunctionalTest extends AbstractSharedLibraryConfigurationFunctionalTest {
	@Override
	protected String getCompileTaskName() {
		return "compileObjectiveC"
	}

	@Override
	protected void makeSingleProject() {
		settingsFile << '''
			rootProject.name = 'jni-greeter'
		'''
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.objective-c-language'
			}

			library.variants.configureEach {
				sharedLibrary.linkTask.configure {
					linkerArgs.add('-lobjc')
				}
			}
		'''
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniObjectiveCGreeterLib('jni-greeter').withOptionalFeature()
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class JavaObjectiveCppJniLibrarySharedLibraryConfigurationFunctionalTest extends AbstractSharedLibraryConfigurationFunctionalTest {
	@Override
	protected String getCompileTaskName() {
		return "compileObjectiveCpp"
	}

	@Override
	protected void makeSingleProject() {
		settingsFile << "rootProject.name = 'jni-greeter'"
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.objective-cpp-language'
			}

			library.variants.configureEach {
				sharedLibrary.linkTask.configure {
					linkerArgs.add('-lobjc')
				}
			}
		'''
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniObjectiveCppGreeterLib('jni-greeter').withOptionalFeature()
	}
}

