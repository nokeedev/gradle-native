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
package dev.nokee.language.c;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.testers.*;
import dev.nokee.language.c.internal.plugins.CSourceSetRegistrationFactory;
import dev.nokee.language.c.internal.plugins.CSourceSetSpec;
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.c.tasks.CCompile;
import dev.nokee.language.nativebase.*;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.testers.HasPublicTypeTester;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
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

@PluginRequirement.Require(id = "dev.nokee.c-language-base")
@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
class CSourceSetIntegrationTest extends AbstractPluginTest implements LanguageSourceSetTester
	, HasPublicTypeTester<CSourceSet>
	, HasConfigurableSourceTester
	, HasConfigurableHeadersTester
	, HasCompileTaskTester
	, LanguageSourceSetHasBuildableSourceIntegrationTester<CSourceSetSpec>
	, LanguageSourceSetHasBuildableHeadersIntegrationTester<CSourceSetSpec>
	, LanguageSourceSetHasBuildableCompileTaskIntegrationTester<CSourceSetSpec>
	, LanguageSourceSetHasCompiledSourceIntegrationTester<CSourceSetSpec>
	, LanguageSourceSetHasCompiledHeadersIntegrationTester<CSourceSetSpec>
	, LanguageSourceSetNativeCompileTaskIntegrationTester<CSourceSetSpec>
	, LanguageSourceSetHasCompiledHeaderSearchPathsIntegrationTester<CSourceSetSpec>
{
	private DomainObjectProvider<CSourceSetSpec> subject;

	@BeforeEach
	void createSubject() {
		subject = project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(CSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "nopu"))).as(CSourceSetSpec.class);
	}

	@Override
	public CSourceSetSpec subject() {
		return subject.get();
	}

	@Override
	public Configuration headerSearchPaths() {
		return subject.element("headerSearchPaths", Configuration.class).get();
	}

	@Test
	public void hasName() {
		assertThat(subject(), named("nopu"));
	}

	@Test
	void hasToString() {
		assertThat(subject(), Matchers.hasToString("C sources 'nopu'"));
	}

	@Test
	public void hasCompileTask() {
		assertThat(subject().getCompileTask(), providerOf(isA(CCompile.class)));
	}

	@Nested
	class SourceSetTest extends NativeLanguageSourceSetIntegrationTester<CSourceSet> {
		@BeforeEach
		public void configureTargetPlatform() {
			((CCompileTask) project.getTasks().getByName("compileNopu")).getTargetPlatform().set(create(of("macos-x64")));
		}

		@Override
		public CSourceSet subject() {
			return subject.get();
		}

		@Override
		public Project project() {
			return project;
		}

		@Override
		public String variantName() {
			return "nopu";
		}
	}
}
