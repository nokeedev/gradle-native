/*
 * Copyright 2024 the original author or authors.
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

package dev.nokee.platform.nativebase.fixtures;

import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;
import dev.nokee.platform.jni.fixtures.elements.CppGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.NativeElements.lib;
import static dev.gradleplugins.fixtures.sources.NativeElements.subproject;

public final class CppGreeterApp extends GreeterImplementationAwareSourceElement {
	@Override
	public SourceElement getElementUsingGreeter() {
		return new CppMainUsesGreeter();
	}

	@Override
	public SourceElement getGreeter() {
		return new CppGreeter();
	}

	@Override
	public ImplementationAsSubprojectElement withImplementationAsSubproject(String subprojectPath) {
		return new ImplementationAsSubprojectElement(getElementUsingGreeter(), getGreeter().as(lib()).as(subproject(subprojectPath)));
	}

	public SourceElement withGenericTestSuite() {
		return ofElements(getElementUsingGreeter(), getGreeter(), new CppGreeterTest());
	}

	private static class CppMainUsesGreeter extends SourceFileElement {
		@Override
		@SourceFileLocation(file = "cpp-greeter-app/src/main/cpp/main.cpp")
		public SourceFile getSourceFile() {
			return sourceFile("cpp", "main.cpp", fromResource("cpp-greeter-app/main.cpp"));
		}
	}
}
