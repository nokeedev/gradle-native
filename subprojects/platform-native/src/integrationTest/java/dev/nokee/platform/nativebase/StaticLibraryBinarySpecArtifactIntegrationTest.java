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
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.testers.ArtifactTester;
import dev.nokee.platform.nativebase.internal.StaticLibraryBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;

import static dev.nokee.platform.nativebase.NativePlatformTestUtils.macosPlatform;

@PluginRequirement.Require(type = NativeComponentBasePlugin.class)
@IntegrationTest
class StaticLibraryBinarySpecArtifactIntegrationTest implements ArtifactTester<StaticLibraryBinary> {
	@GradleProject Project project;
	StaticLibraryBinary subject;

	@BeforeEach
	void createSubject() {
		val factory = project.getExtensions().getByType(StaticLibraryBinaryRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val projectIdentifier = ProjectIdentifier.of(project);
		subject = registry.register(factory.create(projectIdentifier.child("wuxo"))).as(StaticLibraryBinary.class).get();
		subject.getCreateTask().configure(task -> ((CreateStaticLibraryTask) task).getTargetPlatform().set(macosPlatform()));
	}

	@Override
	public StaticLibraryBinary subject() {
		return subject;
	}
}
