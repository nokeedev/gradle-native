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
package dev.nokee.platform.nativebase;

import dev.nokee.internal.testing.IntegrationTest;
import dev.nokee.internal.testing.junit.jupiter.GradleProject;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.ConfigurationMatchers.description;
import static dev.nokee.internal.testing.ConfigurationMatchers.resolvable;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

@IntegrationTest
abstract class NativeRuntimeLibrariesConfigurationIntegrationTester<T extends NativeBinary> {
	@GradleProject protected Project project;
	Configuration subject;

	public abstract T createSubject(String name);

	@BeforeEach
	void initializeSubject() {
		createSubject("doko");
		this.subject = realize(project.getConfigurations().getByName("dokoRuntimeLibraries"));
	}

	private static Configuration realize(Configuration self) {
		((ConfigurationInternal) self).preventFromFurtherMutation();
		return self;
	}

	@Test
	void hasDescription() {
		assertThat(subject, description("Runtime libraries for binary ':doko'."));
	}

	@Test
	void isResolvable() {
		assertThat(subject, resolvable());
	}

	@Test
	void hasNativeLinkUsage() {
		assertThat(subject, attributes(hasEntry(is(Usage.USAGE_ATTRIBUTE), named("native-runtime"))));
	}
}
