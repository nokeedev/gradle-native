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
package dev.nokee.utils;

import lombok.val;
import org.gradle.api.file.ConfigurableFileCollection;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.internal.testing.FileSystemMatchers.aFileNamed;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.utils.FileCollectionUtils.elementsOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class FileCollectionUtils_ElementsOfTest {
	@Test
	void canGetFileCollectionElements() {
		val subject = objectFactory().newInstance(TestComponent.class);
		val provider = providerFactory().provider(() -> subject);
		subject.getSources().from("file.txt");
		assertThat(provider.flatMap(elementsOf(TestComponent::getSources)), providerOf(contains(aFileNamed("file.txt"))));
	}

	@Test
	void checkToString() {
		assertThat(elementsOf(TestComponent::getSources), hasToString(startsWith("FileCollectionUtils.elementsOf(")));
	}

	interface TestComponent {
		ConfigurableFileCollection getSources();
	}
}
