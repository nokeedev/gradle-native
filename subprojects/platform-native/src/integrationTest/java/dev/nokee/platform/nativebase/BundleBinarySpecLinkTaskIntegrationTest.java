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
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.nativebase.internal.BundleBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.tasks.internal.LinkBundleTask;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.aFileNamed;
import static dev.nokee.internal.testing.FileSystemMatchers.parentFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.presentProvider;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.platform.nativebase.NativePlatformTestUtils.macosPlatform;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@IntegrationTest
@PluginRequirement.Require(type = NativeComponentBasePlugin.class)
class BundleBinarySpecLinkTaskIntegrationTest {
	@GradleProject Project project;
	BundleBinary binary;
	LinkBundleTask subject;

	@BeforeEach
	void createSubject() {
		val factory = project.getExtensions().getByType(BundleBinaryRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val projectIdentifier = ProjectIdentifier.of(project);
		val componentIdentifier = ComponentIdentifier.of("luko", projectIdentifier);
		registry.register(ModelRegistration.builder().withComponent(new IdentifierComponent(componentIdentifier)).build());
		val variantIdentifier = VariantIdentifier.of("foto", Variant.class, componentIdentifier);
		registry.register(ModelRegistration.builder().withComponent(new IdentifierComponent(variantIdentifier)).build());
		binary = registry.register(factory.create(BinaryIdentifier.of(variantIdentifier, "vupi"))).as(BundleBinary.class).get();

		binary.getLinkTask().configure(task -> ((LinkBundleTask) task).getTargetPlatform().set(create(of("macos-x64"))));
		subject = (LinkBundleTask) binary.getLinkTask().get();
	}

	@Test
	void usesBinaryNameAsTaskNameVariant() {
		assertThat(subject, named("linkLukoFotoVupi"));
	}

	@Test
	void hasLinkerArgs() {
		assertThat("not null as per contract", subject.getLinkerArgs(), notNullValue(ListProperty.class));
		assertThat("there should be a value", subject.getLinkerArgs(), presentProvider());
	}

	@Test
	void hasDescription() {
		assertThat(subject, TaskMatchers.description("Links the binary ':luko:foto:vupi'."));
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
		assertThat(subject.getLinkedFile(), providerOf(aFile(parentFile(is(newDestinationDirectory)))));
	}

	@Test
	void isNotDebuggableByDefault() {
		assertThat(subject.isDebuggable(), is(false));
	}

	@Test
	void hasClangLinkerBundleArgumentsByDefault() {
		assertThat(subject.getLinkerArgs(), providerOf(hasItems("-Xlinker", "bundle")));
	}

	@Test
	void includesBinaryNameInDestinationDirectory() {
		assertThat(subject.getDestinationDirectory(), providerOf(aFileNamed("vupi")));
	}

	@Test
	@EnabledOnOs(OS.MAC)
	@PluginRequirement.Require(type = SwiftCompilerPlugin.class) // only for Swiftc, at the moment
	void addsMacOsSdkPathToLinkerArguments() {
		subject.getTargetPlatform().set(macosPlatform());
		assertThat(subject.getLinkerArgs(), providerOf(hasItem("-sdk")));
	}
}
