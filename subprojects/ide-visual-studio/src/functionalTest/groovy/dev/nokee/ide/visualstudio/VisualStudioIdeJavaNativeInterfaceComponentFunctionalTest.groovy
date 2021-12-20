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

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib
import org.junit.Assume
import spock.lang.Ignore

@Ignore('https://github.com/nokeedev/gradle-native/issues/489')
class VisualStudioIdeJavaNativeInterfaceLibraryFunctionalTest extends AbstractVisualStudioIdeNativeComponentPluginFunctionalTest implements CppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
				id 'dev.nokee.visual-studio-ide'
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniCppGreeterLib('lib')
	}

	@Override
	protected String configureCustomSourceLayout() {
		Assume.assumeFalse(true)
		throw new UnsupportedOperationException()
	}

	@Override
	protected String configureBuildTypes(String... buildTypes) {
		Assume.assumeFalse(true)
		return super.configureBuildTypes(buildTypes)
	}

	@Override
	protected String getVisualStudioProjectName() {
		return 'lib'
	}

	@Override
	protected List<String> allTasksForBuildAction(String variant) {
		return [':compileJava'] + tasks.withOperatingSystemFamily(variant).allToLink
	}

	@Override
	protected List<String> getGeneratedHeaders() {
		return ['com_example_greeter_Greeter.h']
	}
}
