/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal.components;

import dev.nokee.model.internal.core.ModelComponent;
import org.gradle.util.Path;

public final class GradleProjectPathComponent implements ModelComponent {
	private final Path value;

	public GradleProjectPathComponent(Path value) {
		this.value = value;
	}

	public Path get() {
		return value;
	}
}
