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
import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.internal.testing.junit.jupiter.GradleProject;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.nativebase.internal.NativeStaticLibraryBinarySpec;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.aFileNamed;
import static dev.nokee.internal.testing.FileSystemMatchers.parentFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.presentProvider;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@PluginRequirement.Require(type = NativeComponentBasePlugin.class)
@IntegrationTest
class StaticLibraryBinarySpecCreateTaskIntegrationTest {
	@GradleProject Project project;
	StaticLibraryBinary binary;
	CreateStaticLibraryTask subject;

	@BeforeEach
	void createSubject() {
		val projectIdentifier = ProjectIdentifier.of(project);
		val componentIdentifier = ComponentIdentifier.of("vula", projectIdentifier);
		val variantIdentifier = VariantIdentifier.of("nusi", componentIdentifier);
		binary = model(project, registryOf(Artifact.class)).register(variantIdentifier.child("liku"), NativeStaticLibraryBinarySpec.class).get();;

		binary.getCreateTask().configure(task -> ((CreateStaticLibraryTask) task).getTargetPlatform().set(create(of("macos-x64"))));
		subject = (CreateStaticLibraryTask) binary.getCreateTask().get();
	}

	@Test
	void usesBinaryNameAsTaskNameVariant() {
		assertThat(subject, named("createVulaNusiLiku"));
	}

	@Test
	void hasArchiverArgs() {
		assertThat("not null as per contract", subject.getArchiverArgs(), notNullValue(ListProperty.class));
		assertThat("there should be a value", subject.getArchiverArgs(), presentProvider());
	}

	@Test
	void hasDescription() {
		assertThat(subject, TaskMatchers.description("Creates the binary ':vula:nusi:liku'."));
	}

	@Test
	void locksToolChainProperty() {
		assertThrows(RuntimeException.class, () -> subject.getToolChain().set(mock(NativeToolChain.class)));
	}

	@Test
	void hasDestinationDirectoryUnderLibsInsideBuildDirectory() {
		assertThat(subject.getDestinationDirectory(),
			providerOf(aFile(withAbsolutePath(containsString("/build/libs/")))));
	}

	@Test
	void usesDestinationDirectoryAsOutputFileParentDirectory() {
		val newDestinationDirectory = project.file("some-new-destination-directory");
		subject.getDestinationDirectory().set(newDestinationDirectory);
		assertThat(subject.getOutputFile(), providerOf(aFile(parentFile(is(newDestinationDirectory)))));
	}

	@Test
	void hasNoArchiverArgumentsByDefault() {
		assertThat(subject.getArchiverArgs(), providerOf(emptyIterable()));
	}

	@Test
	void includesBinaryNameInDestinationDirectory() {
		assertThat(subject.getDestinationDirectory(), providerOf(aFileNamed("liku")));
	}
}
