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

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.extendsFrom;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

@PluginRequirement.Require(id = "dev.nokee.jni-library")
class JavaNativeInterfaceLibraryMainComponentIntegrationTest extends AbstractPluginTest {
	private JavaNativeInterfaceLibrary subject;

	@BeforeEach
	void createSubject() {
		this.subject = project.getExtensions().getByType(JavaNativeInterfaceLibrary.class);
	}

	@Test
	void usesProjectNameAsComponentBaseNameConvention() {
		subject.getBaseName().set((String) null);
		assertThat(subject.getBaseName(), providerOf(project.getName()));
	}

	@Nested
	@PluginRequirement.Require(id = "java")
	class WhenJavaPluginApplied {
	}
}
