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
package dev.nokee.language.swift;

import dev.nokee.internal.testing.ConfigurationMatchers;
import dev.nokee.language.base.testers.LanguageSourceSetIntegrationTester;
import dev.nokee.language.nativebase.NativeCompileTaskTester;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

public abstract class SwiftSourceSetIntegrationTester extends LanguageSourceSetIntegrationTester<SwiftSourceSet> {
	public abstract String name();

	@Nested
	class ImportModulesConfigurationTest {
		public Configuration subject() {
			return project().getConfigurations().getByName(variantName() + "ImportModules");
		}

		@Test
		void isResolvable() {
			assertThat(subject(), ConfigurationMatchers.resolvable());
		}

		@Test
		void hasSwiftApiUsage() {
			assertThat(subject(), ConfigurationMatchers.attributes(hasEntry(is(Usage.USAGE_ATTRIBUTE), named("swift-api"))));
		}

		@Test
		void hasDescription() {
			assertThat(subject(), ConfigurationMatchers.description("Import modules for " + displayName() + "."));
		}
	}

	@Nested
	class SwiftCompileTaskTest implements SwiftCompileTester, NativeCompileTaskTester {
		public SwiftCompileTask subject() {
			return (SwiftCompileTask) project().getTasks().getByName("compile" + StringUtils.capitalize(variantName()));
		}

		@Override
		public String languageSourceSetName() {
			return name();
		}
	}
}
