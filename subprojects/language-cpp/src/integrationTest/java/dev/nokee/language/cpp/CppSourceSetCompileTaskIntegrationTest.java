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
import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.internal.testing.junit.jupiter.Subject;
import dev.nokee.language.cpp.internal.plugins.CppSourceSetSpec;
import dev.nokee.language.cpp.internal.tasks.CppCompileTask;
import dev.nokee.language.nativebase.NativeCompileTaskObjectFilesTester;
import dev.nokee.language.nativebase.NativeCompileTaskTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sources;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@PluginRequirement.Require(id = "dev.nokee.cpp-language-base")
class CppSourceSetCompileTaskIntegrationTest extends AbstractPluginTest implements CppCompileTester
	, NativeCompileTaskTester
	, NativeCompileTaskObjectFilesTester<CppCompileTask>
{
	@Subject CppCompileTask subject;

	@Override
	public CppCompileTask subject() {
		return subject;
	}

	CppCompileTask createSubject() {
		return sources(project).register("gali", CppSourceSetSpec.class)
			.flatMap(CppSourceSetSpec::getCompileTask).get();
	}

	@BeforeEach
	void configureTargetPlatform() {
		subject.getTargetPlatform().set(create(of("macos-x64")));
	}

	@Test
	void hasName() {
		assertThat(subject, named("compileGali"));
	}

	@Test
	void hasDescription() {
		assertThat(subject, TaskMatchers.description("Compiles the sources ':gali'."));
	}

	@Test
	void enablesPositionIndependentCodeByDefault() {
		assertThat(subject.isPositionIndependentCode(), is(true));
	}
}
