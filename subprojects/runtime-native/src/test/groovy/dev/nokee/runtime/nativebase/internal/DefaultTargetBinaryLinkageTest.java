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
package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.TargetLinkage;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.utils.ConfigurationUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class DefaultTargetBinaryLinkageTest {
	TargetLinkage createSubject(String name) {
		return new DefaultTargetLinkage(BinaryLinkage.named(name));
	}

	@Test
	void doesNotProvideAttributeForConsumableConfiguration() {
		val project = rootProject();
		val test = project.getConfigurations().create("test",
			configureAsConsumable().andThen(configureAttributes(attributesOf(createSubject("my-linkage")))));
		assertThat(test, attributes(not(hasEntry(equalTo(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE), named("my-linkage")))));
	}

	@Test
	void providesAttributeForResolvableConfiguration() {
		val project = rootProject();
		val test = project.getConfigurations().create("test",
			configureAsResolvable().andThen(configureAttributes(attributesOf(createSubject("my-linkage")))));
		assertThat(test, attributes(hasEntry(equalTo(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE), named("my-linkage"))));
	}

	@Test
	void hasToStringName() {
		assertThat(createSubject("my-linkage"), hasToString("my-linkage"));
	}
}
