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
package dev.nokee.language.cpp;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.Subject;
import dev.nokee.language.base.testers.HasCompileTaskTester;
import dev.nokee.language.base.testers.HasConfigurableSourceTester;
import dev.nokee.language.base.testers.LanguageSourceSetHasBuildableCompileTaskIntegrationTester;
import dev.nokee.language.base.testers.LanguageSourceSetHasBuildableSourceIntegrationTester;
import dev.nokee.language.base.testers.LanguageSourceSetTester;
import dev.nokee.language.cpp.internal.plugins.CppSourceSetSpec;
import dev.nokee.language.cpp.tasks.CppCompile;
import dev.nokee.language.nativebase.HasConfigurableHeadersTester;
import dev.nokee.language.nativebase.LanguageSourceSetHasBuildableHeadersIntegrationTester;
import dev.nokee.language.nativebase.LanguageSourceSetHasCompiledHeaderSearchPathsIntegrationTester;
import dev.nokee.language.nativebase.LanguageSourceSetHasCompiledHeadersIntegrationTester;
import dev.nokee.language.nativebase.LanguageSourceSetHasCompiledSourceIntegrationTester;
import dev.nokee.language.nativebase.LanguageSourceSetNativeCompileTaskIntegrationTester;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.testers.HasPublicTypeTester;
import org.gradle.api.artifacts.Configuration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.platform.base.internal.DomainObjectEntities.newEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

@PluginRequirement.Require(id = "dev.nokee.cpp-language-base")
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
		return project.getExtensions().getByType(ModelRegistry.class).register(newEntity("zomi", CppSourceSetSpec.class)).as(CppSourceSetSpec.class);
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
