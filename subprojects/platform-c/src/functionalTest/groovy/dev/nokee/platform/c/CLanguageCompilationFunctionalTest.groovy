/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.c

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.fixtures.AbstractNativeLanguageCompilationFunctionalTest
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.c.CTaskNames
import dev.nokee.platform.nativebase.fixtures.CGreeterApp
import dev.nokee.platform.nativebase.fixtures.CGreeterLib

class CApplicationNativeLanguageCompilationFunctionalTest extends AbstractNativeLanguageCompilationFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.c-application'
			}
		'''
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new CGreeterApp()
	}

	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'executable'
	}
}

class CLibraryNativeLanguageCompilationFunctionalTest extends AbstractNativeLanguageCompilationFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.c-library'
			}
		'''
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new CGreeterLib()
	}

	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'sharedLibrary'
	}
}

class CLibraryWithStaticLinkageNativeLanguageCompilationFunctionalTest extends CLibraryNativeLanguageCompilationFunctionalTest {
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
	protected String getBinaryLifecycleTaskName() {
		return 'staticLibrary'
	}

	@Override
	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks.forStaticLibrary
	}
}

class CLibraryWithSharedLinkageNativeLanguageCompilationFunctionalTest extends CLibraryNativeLanguageCompilationFunctionalTest {
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

class CLibraryWithBothLinkageNativeLanguageCompilationFunctionalTest extends CLibraryNativeLanguageCompilationFunctionalTest {
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
	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks.withLinkage('shared')
	}
}
