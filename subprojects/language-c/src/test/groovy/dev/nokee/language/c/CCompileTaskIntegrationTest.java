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
package dev.nokee.language.c;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.c.internal.plugins.CSourceSetRegistrationFactory;
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;

@PluginRequirement.Require(id = "dev.nokee.c-language-base")
class CCompileTaskIntegrationTest extends AbstractPluginTest implements CCompileTester {
	private CCompileTask subject;

	@Override
	public CCompileTask subject() {
		return subject;
	}

	@BeforeEach
	void createSubject() {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);
		subject = project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(CSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "mufa"))).element("compile", CCompileTask.class).get();
		subject.getTargetPlatform().set(create(of("macos-x64")));
	}

	@Test
	void hasName() {
		assertThat(subject, named("compileMufa"));
	}

	@Test
	void hasDescription() {
		assertThat(subject, TaskMatchers.description("Compiles the sources ':mufa'."));
	}
}
