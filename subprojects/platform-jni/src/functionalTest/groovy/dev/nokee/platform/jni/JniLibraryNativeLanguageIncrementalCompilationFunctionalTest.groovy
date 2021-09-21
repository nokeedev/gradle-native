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

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.fixtures.AbstractNativeLanguageIncrementalCompilationFunctionalTest
import dev.nokee.language.c.CTaskNames
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.language.objectivec.ObjectiveCTaskNames
import dev.nokee.language.objectivecpp.ObjectiveCppTaskNames
import dev.nokee.platform.jni.fixtures.JavaJniTaskNames
import spock.lang.Requires
import spock.util.environment.OperatingSystem

abstract class AbstractJniLibraryNativeLanguageIncrementalCompilationFunctionalTest extends AbstractNativeLanguageIncrementalCompilationFunctionalTest {
	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'sharedLibrary'
	}

	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id '${jvmLanguagePluginId}'
				id 'dev.nokee.jni-library'
				id '${nativeLanguagePluginId}'
			}
		"""
	}

	@Override
	protected List<String> getAllSkippedTasksToAssemble() {
		return tasks.allToAssemble - [':jar', ':assemble']
	}

	protected String getJvmLanguagePluginId() {
		String className = this.getClass().simpleName
		if (className.contains('Java')) {
			return 'java'
		}
		throw new IllegalArgumentException('Unable to figure out the JVM language plugin to use')
	}

	protected String getNativeLanguagePluginId() {
		String className = this.getClass().simpleName
		if (className.contains('ObjectiveCppLanguage')) {
			return 'dev.nokee.objective-cpp-language'
		} else if (className.contains('ObjectiveCLanguage')) {
			return 'dev.nokee.objective-c-language'
		} else if (className.contains('CLanguage')) {
			return 'dev.nokee.c-language'
		} else if (className.contains('CppLanguage')) {
			return 'dev.nokee.cpp-language'
		}
		throw new IllegalArgumentException('Unable to figure out the native language plugin to use')
	}
}

class JavaJniLibraryCLanguageIncrementalCompilationFunctionalTest extends AbstractJniLibraryNativeLanguageIncrementalCompilationFunctionalTest implements CTaskNames, JavaJniTaskNames {
}

class JavaJniLibraryCppLanguageIncrementalCompilationFunctionalTest extends AbstractJniLibraryNativeLanguageIncrementalCompilationFunctionalTest implements CppTaskNames, JavaJniTaskNames {
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class JavaJniLibraryObjectiveCLanguageIncrementalCompilationFunctionalTest extends AbstractJniLibraryNativeLanguageIncrementalCompilationFunctionalTest implements ObjectiveCTaskNames, JavaJniTaskNames {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << """
			library.variants.configureEach {
				sharedLibrary.linkTask.configure {
					linkerArgs.add('-lobjc')
				}
			}
		"""
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class JavaJniLibraryObjectiveCppLanguageIncrementalCompilationFunctionalTest extends AbstractJniLibraryNativeLanguageIncrementalCompilationFunctionalTest implements ObjectiveCppTaskNames, JavaJniTaskNames {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << """
			library.variants.configureEach {
				sharedLibrary.linkTask.configure {
					linkerArgs.add('-lobjc')
				}
			}
		"""
	}
}
