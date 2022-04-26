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

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.nativebase.internal.StaticLibraryBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import lombok.val;
import org.gradle.api.provider.ListProperty;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.aFileBaseNamed;
import static dev.nokee.internal.testing.FileSystemMatchers.aFileNamed;
import static dev.nokee.internal.testing.FileSystemMatchers.parentFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.presentProvider;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@PluginRequirement.Require(type = NativeComponentBasePlugin.class)
class StaticLibraryBinarySpecCreateTaskIntegrationTest extends AbstractPluginTest {
	StaticLibraryBinary binary;
	CreateStaticLibraryTask subject;

	@BeforeEach
	void createSubject() {
		val factory = project.getExtensions().getByType(StaticLibraryBinaryRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val projectIdentifier = ProjectIdentifier.of(project);
		val componentIdentifier = ComponentIdentifier.of("vula", projectIdentifier);
		registry.register(ModelRegistration.builder().withComponent(new IdentifierComponent(componentIdentifier)).build());
		val variantIdentifier = VariantIdentifier.of("nusi", Variant.class, componentIdentifier);
		registry.register(ModelRegistration.builder().withComponent(new IdentifierComponent(variantIdentifier)).build());
		binary = registry.register(factory.create(BinaryIdentifier.of(variantIdentifier, "liku"))).as(StaticLibraryBinary.class).get();

		binary.getCreateTask().configure(task -> ((CreateStaticLibraryTask) task).getTargetPlatform().set(create(of("macos-x64"))));
		subject = (CreateStaticLibraryTask) binary.getCreateTask().get();
	}

	@Test
	void usesBinaryNameAsTaskNameVariant() {
		assertThat(subject, named("createVulaNusiLiku"));
	}

	@Test
	void hasLinkerArgs() {
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
	void usesBinaryBaseNameForCreateTaskOutputFileBaseName() {
		binary.getBaseName().set("da-bo");
		assertThat(subject.getOutputFile(), providerOf(aFileBaseNamed(endsWith("da-bo"))));
	}

	@Test
	void usesDestinationDirectoryAsOutputFileParentDirectory() {
		val newDestinationDirectory = project().file("some-new-destination-directory");
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
