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
package dev.nokee.language.objectivec;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.nativebase.NativeLanguageSourceSetIntegrationTester;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCSourceSetRegistrationFactory;
import dev.nokee.language.objectivec.internal.tasks.ObjectiveCCompileTask;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;

@PluginRequirement.Require(id = "dev.nokee.objective-c-language-base")
@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
class ObjectiveCSourceSetIntegrationTest extends AbstractPluginTest {
	private ObjectiveCSourceSet subject;

	@BeforeEach
	void createSubject() {
		subject = project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(ObjectiveCSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "gote"))).as(ObjectiveCSourceSet.class).get();
	}

	@Test
	void hasToString() {
		assertThat(subject, Matchers.hasToString("Objective-C sources 'gote'"));
	}

	@Nested
	class SourceSetTest extends NativeLanguageSourceSetIntegrationTester<ObjectiveCSourceSet> {
		@BeforeEach
		public void configureTargetPlatform() {
			((ObjectiveCCompileTask) project.getTasks().getByName("compileGote")).getTargetPlatform().set(create(of("macos-x64")));
		}

		@Override
		public ObjectiveCSourceSet subject() {
			return subject;
		}

		@Override
		public Project project() {
			return project;
		}

		@Override
		public String name() {
			return "gote";
		}

		@Override
		public String variantName() {
			return "gote";
		}

		@Override
		public String displayName() {
			return "sources ':gote'";
		}

		@Nested
		class ObjectiveCCompileTaskTest implements ObjectiveCCompileTester {
			@Override
			public ObjectiveCCompileTask subject() {
				return (ObjectiveCCompileTask) project().getTasks().getByName("compile" + StringUtils.capitalize(variantName()));
			}
		}
	}
}
