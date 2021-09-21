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
package dev.nokee.runtime.darwin.internal;

import org.gradle.api.attributes.LibraryElements;

import java.util.Objects;

/** @see org.gradle.api.attributes.LibraryElements */
public final class DarwinLibraryElements {
	private DarwinLibraryElements() {}

	/**
	 * Represents a macOS framework bundle for {@link org.gradle.api.attributes.Category#LIBRARY library category}.
	 * A framework bundle usually provide native headers and shared library.
	 */
	public static final String FRAMEWORK_BUNDLE = "framework-bundle";

	public static boolean isFrameworkBundle(LibraryElements self) {
		return Objects.equals(self.getName(), FRAMEWORK_BUNDLE);
	}
}
