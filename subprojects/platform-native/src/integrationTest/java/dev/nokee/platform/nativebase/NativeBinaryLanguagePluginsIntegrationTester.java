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
package dev.nokee.platform.nativebase;

import dev.nokee.internal.testing.IntegrationTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.GradleProject;
import dev.nokee.language.c.tasks.CCompile;
import dev.nokee.language.cpp.tasks.CppCompile;
import dev.nokee.language.objectivec.tasks.ObjectiveCCompile;
import dev.nokee.language.objectivecpp.tasks.ObjectiveCppCompile;
import dev.nokee.language.swift.tasks.SwiftCompile;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;

@IntegrationTest
abstract class NativeBinaryLanguagePluginsIntegrationTester {
	private NativeBinary subject;
	@GradleProject protected Project project;

	public abstract NativeBinary createSubject(String name);

	@BeforeEach
	void createSubject() {
		subject = createSubject("rugu");
	}

	@Test
	void hasNoCompileTasksByDefault() {
		assertThat(subject.getCompileTasks().get(), emptyIterable());
	}

	@Test
	@PluginRequirement.Require(id = "dev.nokee.c-language")
	void hasCCompileTask() {
		assertThat(subject.getCompileTasks().get(),
			hasItem(allOf(named("compileRuguC"), isA(CCompile.class))));
	}

	@Test
	@PluginRequirement.Require(id = "dev.nokee.cpp-language")
	void hasCppCompileTask() {
		assertThat(subject.getCompileTasks().get(),
			hasItem(allOf(named("compileRuguCpp"), isA(CppCompile.class))));
	}

	@Test
	@PluginRequirement.Require(id = "dev.nokee.objective-c-language")
	void hasObjectiveCCompileTask() {
		assertThat(subject.getCompileTasks().get(),
			hasItem(allOf(named("compileRuguObjectiveC"), isA(ObjectiveCCompile.class))));
	}

	@Test
	@PluginRequirement.Require(id = "dev.nokee.objective-cpp-language")
	void hasObjectiveCppCompileTask() {
		assertThat(subject.getCompileTasks().get(),
			hasItem(allOf(named("compileRuguObjectiveCpp"), isA(ObjectiveCppCompile.class))));
	}

	@Test
	@PluginRequirement.Require(id = "dev.nokee.swift-language")
	void hasSwiftCompileTask() {
		assertThat(subject.getCompileTasks().get(),
			hasItem(allOf(named("compileRuguSwift"), isA(SwiftCompile.class))));
	}
}
