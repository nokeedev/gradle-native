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
package dev.nokee.ide.xcode

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib
import org.junit.Assume
import spock.lang.Ignore

@Ignore
class XcodeIdeJavaNativeInterfaceLibraryFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
				id 'dev.nokee.xcode-ide'
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniCppGreeterLib('lib')
	}

	@Override
	protected String configureBuildTypes(String... buildTypes) {
		Assume.assumeFalse(true)
		throw new UnsupportedOperationException()
	}

	protected String configureCustomSourceLayout() {
		Assume.assumeFalse(true)
		throw new UnsupportedOperationException()
	}

	@Override
	protected String getProjectName() {
		return 'lib'
	}

	@Override
	protected List<String> getAllTasksToXcode() {
		return [':compileJava'] + super.allTasksToXcode
	}
}
