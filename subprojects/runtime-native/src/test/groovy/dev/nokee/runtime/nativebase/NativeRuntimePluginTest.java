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
package dev.nokee.runtime.nativebase;

import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import dev.nokee.runtime.nativebase.internal.NativeRuntimePlugin;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class NativeRuntimePluginTest {
	private static Project createSubject() {
		val project = rootProject();
		project.getPluginManager().apply(NativeRuntimePlugin.class);
		return project;
	}

	@Test
	void appliesNativeRuntimeBasePlugin() {
		assertThat(createSubject().getPlugins(), hasItem(isA(NativeRuntimeBasePlugin.class)));
	}

	@Test
	void registerNixStaticLibraryFileExtension() {
		val type = createSubject().getDependencies().getArtifactTypes().getByName("a");
		assertAll(
			() -> assertThat(type, attributes(hasEntry(is(Usage.USAGE_ATTRIBUTE), named(Usage.NATIVE_LINK)))),
			() -> assertThat(type, attributes(hasEntry(is(Category.CATEGORY_ATTRIBUTE), named(Category.LIBRARY)))),
			() -> assertThat(type, attributes(hasEntry(is(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE), named(LibraryElements.LINK_ARCHIVE))))
		);
	}

	@Test
	void registerWindowsStaticLibraryFileExtension() {
		val type = createSubject().getDependencies().getArtifactTypes().getByName("lib");
		assertAll(
			() -> assertThat(type, attributes(hasEntry(is(Usage.USAGE_ATTRIBUTE), named(Usage.NATIVE_LINK)))),
			() -> assertThat(type, attributes(hasEntry(is(Category.CATEGORY_ATTRIBUTE), named(Category.LIBRARY)))),
			() -> assertThat(type, attributes(hasEntry(is(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE), named(LibraryElements.LINK_ARCHIVE))))
		);
	}

	@Test
	void registerNixSharedLibraryFileExtension() {
		val type = createSubject().getDependencies().getArtifactTypes().getByName("so");
		assertAll(
			() -> assertThat(type, attributes(hasEntry(is(Usage.USAGE_ATTRIBUTE), named(Usage.NATIVE_LINK + "+" + Usage.NATIVE_RUNTIME)))),
			() -> assertThat(type, attributes(hasEntry(is(Category.CATEGORY_ATTRIBUTE), named(Category.LIBRARY)))),
			() -> assertThat(type, attributes(hasEntry(is(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE), named(LibraryElements.DYNAMIC_LIB))))
		);
	}

	@Test
	void registerDarwinSharedLibraryFileExtension() {
		val type = createSubject().getDependencies().getArtifactTypes().getByName("dylib");
		assertAll(
			() -> assertThat(type, attributes(hasEntry(is(Usage.USAGE_ATTRIBUTE), named(Usage.NATIVE_LINK + "+" + Usage.NATIVE_RUNTIME)))),
			() -> assertThat(type, attributes(hasEntry(is(Category.CATEGORY_ATTRIBUTE), named(Category.LIBRARY)))),
			() -> assertThat(type, attributes(hasEntry(is(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE), named(LibraryElements.DYNAMIC_LIB))))
		);
	}

	@Test
	void registerWindowsSharedLibraryFileExtension() {
		val type = createSubject().getDependencies().getArtifactTypes().getByName("dll");
		assertAll(
			() -> assertThat(type, attributes(hasEntry(is(Usage.USAGE_ATTRIBUTE), named(Usage.NATIVE_RUNTIME)))),
			() -> assertThat(type, attributes(hasEntry(is(Category.CATEGORY_ATTRIBUTE), named(Category.LIBRARY)))),
			() -> assertThat(type, attributes(hasEntry(is(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE), named(LibraryElements.DYNAMIC_LIB))))
		);
	}
}
