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

import dev.gradleplugins.fixtures.sources.NativeSourceElement;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;

import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;

public final class CppGreeterTest extends NativeSourceElement {
	@Override
	public String getSourceSetName() {
		return "test";
	}

	@Override
	public SourceElement getHeaders() {
		return SourceFileElement.ofFile(sourceFile("headers", "greeter_fixtures.h", fromResource("cpp-greeter-test/greeter_fixtures.h")));
	}

	@Override
	public SourceElement getSources() {
		return SourceFileElement.ofFile(sourceFile("cpp", "greeter_test.cpp", fromResource("cpp-greeter-test/greeter_test.cpp")));
	}
}
