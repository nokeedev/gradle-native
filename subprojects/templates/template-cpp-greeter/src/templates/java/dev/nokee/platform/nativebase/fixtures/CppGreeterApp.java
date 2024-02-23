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

import dev.gradleplugins.fixtures.sources.NativeLibraryElement;
import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.nokee.platform.jni.fixtures.elements.CppGreeter;
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement;

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofNativeElements;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;

public final class CppGreeterApp extends GreeterImplementationAwareSourceElement<NativeLibraryElement/*CppGreeter*/> {
	private final NativeSourceElement delegate;

	public CppGreeterApp() {
		super(new CppMainUsesGreeter(), new CppGreeter());
		delegate = ofNativeElements((NativeSourceElement) getElementUsingGreeter(), getGreeter());
	}

	@Override
	public GreeterImplementationAwareSourceElement<NativeLibraryElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(getElementUsingGreeter(), asSubproject(subprojectPath, getGreeter().asLib()));
	}

	SourceElement withGenericTestSuite() {
		return ofNativeElements(delegate, new CppGreeterTest());
	}

	// TODO: Use SourceFileElement
	private static class CppMainUsesGreeter extends NativeSourceElement {
		@Override
		public SourceElement getSources() {
			return ofFiles(sourceFile("cpp", "main.cpp", fromResource("cpp-greeter-app/main.cpp")));
		}
	}
}
