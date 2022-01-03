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
package dev.nokee.language.objectivecpp;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.testers.*;
import dev.nokee.language.nativebase.HasConfigurableHeadersTester;
import dev.nokee.language.nativebase.LanguageSourceSetHasBuildableHeadersIntegrationTester;
import dev.nokee.language.nativebase.NativeLanguageSourceSetIntegrationTester;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppSourceSetRegistrationFactory;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppSourceSetSpec;
import dev.nokee.language.objectivecpp.internal.tasks.ObjectiveCppCompileTask;
import dev.nokee.language.objectivecpp.tasks.ObjectiveCppCompile;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.testers.HasPublicTypeTester;
import org.gradle.api.Project;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language-base")
@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
class ObjectiveCppSourceSetIntegrationTest extends AbstractPluginTest implements LanguageSourceSetTester
	, HasPublicTypeTester<ObjectiveCppSourceSet>
	, HasConfigurableSourceTester
	, HasConfigurableHeadersTester
	, HasCompileTaskTester
	, LanguageSourceSetHasBuildableSourceIntegrationTester<ObjectiveCppSourceSetSpec>
	, LanguageSourceSetHasBuildableHeadersIntegrationTester<ObjectiveCppSourceSetSpec>
	, LanguageSourceSetHasBuildableCompileTaskIntegrationTester<ObjectiveCppSourceSetSpec>
{
	private ObjectiveCppSourceSetSpec subject;

	@BeforeEach
	void createSubject() {
		subject = project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(ObjectiveCppSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "suhu"))).as(ObjectiveCppSourceSetSpec.class).get();
	}

	@Override
	public ObjectiveCppSourceSetSpec subject() {
		return subject;
	}

	@Test
	public void hasName() {
		assertThat(subject, named("suhu"));
	}

	@Test
	void hasToString() {
		assertThat(subject, Matchers.hasToString("Objective-C++ sources 'suhu'"));
	}

	@Test
	public void hasCompileTask() {
		assertThat(subject.getCompileTask(), providerOf(isA(ObjectiveCppCompile.class)));
	}

	@Nested
	class SourceSetTest extends NativeLanguageSourceSetIntegrationTester<ObjectiveCppSourceSet> {
		@BeforeEach
		public void configureTargetPlatform() {
			((ObjectiveCppCompileTask) project.getTasks().getByName("compileSuhu")).getTargetPlatform().set(create(of("macos-x64")));
		}

		@Override
		public ObjectiveCppSourceSet subject() {
			return subject;
		}

		@Override
		public Project project() {
			return project;
		}

		@Override
		public String name() {
			return "suhu";
		}

		@Override
		public String variantName() {
			return "suhu";
		}
	}
}
