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
package dev.nokee.language.cpp;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.Subject;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.testers.*;
import dev.nokee.language.cpp.internal.plugins.CppSourceSetRegistrationFactory;
import dev.nokee.language.cpp.internal.plugins.CppSourceSetSpec;
import dev.nokee.language.cpp.tasks.CppCompile;
import dev.nokee.language.nativebase.*;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.testers.HasPublicTypeTester;
import org.gradle.api.artifacts.Configuration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

@PluginRequirement.Require(id = "dev.nokee.cpp-language-base")
@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
class CppSourceSetIntegrationTest extends AbstractPluginTest implements LanguageSourceSetTester
	, HasPublicTypeTester<CppSourceSet>
	, HasConfigurableSourceTester
	, HasConfigurableHeadersTester
	, HasCompileTaskTester
	, LanguageSourceSetHasBuildableSourceIntegrationTester<CppSourceSetSpec>
	, LanguageSourceSetHasBuildableHeadersIntegrationTester<CppSourceSetSpec>
	, LanguageSourceSetHasBuildableCompileTaskIntegrationTester<CppSourceSetSpec>
	, LanguageSourceSetHasCompiledSourceIntegrationTester<CppSourceSetSpec>
	, LanguageSourceSetHasCompiledHeadersIntegrationTester<CppSourceSetSpec>
	, LanguageSourceSetNativeCompileTaskIntegrationTester<CppSourceSetSpec>
	, LanguageSourceSetHasCompiledHeaderSearchPathsIntegrationTester<CppSourceSetSpec>
{
	@Subject DomainObjectProvider<CppSourceSetSpec> subject;

	DomainObjectProvider<CppSourceSetSpec> createSubject() {
		return project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(CppSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "zomi"))).as(CppSourceSetSpec.class);
	}

	@Override
	public CppSourceSetSpec subject() {
		return subject.get();
	}

	@Override
	public Configuration headerSearchPaths() {
		return subject.element("headerSearchPaths", Configuration.class).get();
	}

	@Test
	public void hasName() {
		assertThat(subject(), named("zomi"));
	}

	@Test
	void hasToString() {
		assertThat(subject(), Matchers.hasToString("C++ sources 'zomi'"));
	}

	@Test
	public void hasCompileTask() {
		assertThat(subject().getCompileTask(), providerOf(isA(CppCompile.class)));
	}
}
