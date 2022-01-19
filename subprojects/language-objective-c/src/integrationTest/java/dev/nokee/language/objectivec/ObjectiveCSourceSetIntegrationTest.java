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
package dev.nokee.language.objectivec;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.Subject;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.testers.*;
import dev.nokee.language.nativebase.*;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCSourceSetRegistrationFactory;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCSourceSetSpec;
import dev.nokee.language.objectivec.tasks.ObjectiveCCompile;
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

@PluginRequirement.Require(id = "dev.nokee.objective-c-language-base")
class ObjectiveCSourceSetIntegrationTest extends AbstractPluginTest implements LanguageSourceSetTester
	, HasPublicTypeTester<ObjectiveCSourceSet>
	, HasConfigurableSourceTester
	, HasConfigurableHeadersTester
	, HasCompileTaskTester
	, LanguageSourceSetHasBuildableSourceIntegrationTester<ObjectiveCSourceSetSpec>
	, LanguageSourceSetHasBuildableHeadersIntegrationTester<ObjectiveCSourceSetSpec>
	, LanguageSourceSetHasBuildableCompileTaskIntegrationTester<ObjectiveCSourceSetSpec>
	, LanguageSourceSetHasCompiledSourceIntegrationTester<ObjectiveCSourceSetSpec>
	, LanguageSourceSetHasCompiledHeadersIntegrationTester<ObjectiveCSourceSetSpec>
	, LanguageSourceSetNativeCompileTaskIntegrationTester<ObjectiveCSourceSetSpec>
	, LanguageSourceSetHasCompiledHeaderSearchPathsIntegrationTester<ObjectiveCSourceSetSpec>
{
	@Subject DomainObjectProvider<ObjectiveCSourceSetSpec> subject;

	DomainObjectProvider<ObjectiveCSourceSetSpec> createSubject() {
		return project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(ObjectiveCSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "gote"))).as(ObjectiveCSourceSetSpec.class);
	}

	@Override
	public ObjectiveCSourceSetSpec subject() {
		return subject.get();
	}

	@Override
	public Configuration headerSearchPaths() {
		return subject.element("headerSearchPaths", Configuration.class).get();
	}

	@Test
	public void hasName() {
		assertThat(subject(), named("gote"));
	}

	@Test
	void hasToString() {
		assertThat(subject(), Matchers.hasToString("Objective-C sources 'gote'"));
	}

	@Test
	public void hasCompileTask() {
		assertThat(subject().getCompileTask(), providerOf(isA(ObjectiveCCompile.class)));
	}
}
