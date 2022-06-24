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
import dev.nokee.internal.testing.junit.jupiter.Subject;
import dev.nokee.language.c.internal.plugins.CSourceSetSpec;
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.nativebase.NativeCompileTaskObjectFilesTester;
import dev.nokee.language.nativebase.NativeCompileTaskTester;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.model.internal.DomainObjectEntities.newEntity;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@PluginRequirement.Require(id = "dev.nokee.c-language-base")
class CSourceSetCompileTaskIntegrationTest extends AbstractPluginTest implements CCompileTester
	, NativeCompileTaskTester
	, NativeCompileTaskObjectFilesTester<CCompileTask>
{
	@Subject CCompileTask subject;

	@Override
	public CCompileTask subject() {
		return subject;
	}

	CCompileTask createSubject() {
		return project.getExtensions().getByType(ModelRegistry.class).register(newEntity("mufa", CSourceSetSpec.class).build()).element("compile", CCompileTask.class).get();
	}

	@BeforeEach
	void configureTargetPlatform() {
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

	@Test
	void enablesPositionIndependentCodeByDefault() {
		assertThat(subject.isPositionIndependentCode(), is(true));
	}
}
