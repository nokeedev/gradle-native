/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.internal.testing.runnerkit;

public enum GradleDsl {
	GROOVY("gradle") {
		@Override
		Section newSection(Object text) {
			return new GenericSection(
				() -> text.toString(),
				() -> { throw new UnsupportedOperationException("No Kotlin code equivalent"); }
			);
		}
	},
	KOTLIN("gradle.kts") {
		@Override
		Section newSection(Object text) {
			return new GenericSection(
				() -> { throw new UnsupportedOperationException("No Groovy code equivalent"); },
				() -> text.toString()
			);
		}
	};

	private final String fileExtension;

	GradleDsl(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public String fileName(String pathWithoutExtension) {
		return pathWithoutExtension + "." + fileExtension;
	}

	abstract Section newSection(Object text);
}
