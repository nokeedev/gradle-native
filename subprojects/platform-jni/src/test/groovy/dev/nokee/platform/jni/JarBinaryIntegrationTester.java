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
package dev.nokee.platform.jni;

import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.model.testers.HasPublicTypeTester;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public abstract class JarBinaryIntegrationTester<T extends JarBinary> implements JarBinaryTester<T>, HasPublicTypeTester<T> {
	public abstract String variantName();

	public abstract Project project();

	private Jar jarTask() {
		return (Jar) project().getTasks().getByName("jar" + capitalize(variantName()));
	}

	@Nested
	class JarTaskPropertyTest {
		public TaskProvider<Jar> subject() {
			return JarBinaryIntegrationTester.this.subject().getJarTask();
		}

		@Test
		void hasJarTaskWithProperName() {
			assertThat(subject(), providerOf(named("jar" + capitalize(variantName()))));
		}
	}

	@Nested
	class BaseJarTaskTest {
		public Jar subject() {
			return jarTask();
		}

		@Test
		void hasDescription() {
			assertThat(subject().getDescription(), notNullValue(String.class));
		}

		@Test
		void hasBuildGroup() {
			assertThat(subject().getGroup(), equalTo("build"));
		}
	}
}
