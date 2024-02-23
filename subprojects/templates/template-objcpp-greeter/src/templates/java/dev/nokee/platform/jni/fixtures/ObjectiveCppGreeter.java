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

package dev.nokee.platform.jni.fixtures;

import dev.gradleplugins.fixtures.sources.NativeLibraryElement;
import dev.gradleplugins.fixtures.sources.SourceElement;

import static dev.gradleplugins.fixtures.sources.SourceFileElement.fromResource;
import static dev.gradleplugins.fixtures.sources.SourceFileElement.ofFile;

public final class ObjectiveCppGreeter extends NativeLibraryElement {
	private final SourceElement header;
	private final SourceElement source;

	@Override
    public SourceElement getPublicHeaders() {
		return header;
	}

	@Override
	public SourceElement getSources() {
		return source;
	}

    public ObjectiveCppGreeter() {
		header = ofFile(sourceFile("headers", "greeter.h", fromResource("objcpp-greeter/greeter.h")));
		source = ofFile(sourceFile("objcpp", "greeter_impl.mm", fromResource("objcpp-greeter/greeter_impl.mm")));
	}

	public NativeLibraryElement withOptionalFeature() {
		return new NativeLibraryElement() {
			@Override
			public SourceElement getPublicHeaders() {
				return header;
			}

			@Override
			public SourceElement getSources() {
				return ofFile(sourceFile("objcpp", "greeter_impl.mm", fromResource("objcpp-greeter-with-optional-feature/greeter_impl.mm")));
			}
		};
	}
}
