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

package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceElements;
import dev.gradleplugins.fixtures.sources.SourceFile;

import java.util.Arrays;
import java.util.List;

public abstract class GreeterImplementationAwareSourceElement extends SourceElement {
	@Override
	public List<SourceFile> getFiles() {
		return ofElements(getElementUsingGreeter(), getGreeter()).getFiles();
	}

	public abstract SourceElement getElementUsingGreeter();

	public abstract SourceElement getGreeter();

	public abstract ImplementationAsSubprojectElement withImplementationAsSubproject(String subprojectPath);

	public static final class ImplementationAsSubprojectElement extends SourceElements {
		private final SourceElement elementUsingGreeter;
		private final SourceElement greeter;

		public ImplementationAsSubprojectElement(SourceElement elementUsingGreeter, SourceElement greeter) {
			this.elementUsingGreeter = elementUsingGreeter;
			this.greeter = greeter;
		}

		@Override
		public List<SourceElement> getElements() {
			return Arrays.asList(elementUsingGreeter, greeter);
		}

		public SourceElement getElementUsingGreeter() {
			return elementUsingGreeter;
		}

		public SourceElement getGreeter() {
			return greeter;
		}
	}
}
