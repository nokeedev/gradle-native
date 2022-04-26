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
import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.provider.ListProperty;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.Files;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.aFileBaseNamed;
import static dev.nokee.internal.testing.FileSystemMatchers.aFileNamed;
import static dev.nokee.internal.testing.FileSystemMatchers.parentFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.absentProvider;
import static dev.nokee.internal.testing.GradleProviderMatchers.presentProvider;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.util.ProjectTestUtils.createDependency;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.platform.nativebase.NativePlatformTestUtils.macosPlatform;
import static dev.nokee.platform.nativebase.NativePlatformTestUtils.nixPlatform;
import static dev.nokee.platform.nativebase.NativePlatformTestUtils.windowsPlatform;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static dev.nokee.utils.ConfigurationUtils.configureAsConsumable;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.ConfigurationUtils.forUsage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@PluginRequirement.Require(type = NativeComponentBasePlugin.class)
@IntegrationTest
class SharedLibraryBinarySpecLinkTaskIntegrationTest {
	@GradleProject Project project;
	SharedLibraryBinary binary;
	LinkSharedLibraryTask subject;

	@BeforeEach
	void createSubject() {
		val factory = project.getExtensions().getByType(SharedLibraryBinaryRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val projectIdentifier = ProjectIdentifier.of(project);
		val componentIdentifier = ComponentIdentifier.of("qame", projectIdentifier);
		registry.register(ModelRegistration.builder().withComponent(new IdentifierComponent(componentIdentifier)).build());
		val variantIdentifier = VariantIdentifier.of("sopu", Variant.class, componentIdentifier);
		registry.register(ModelRegistration.builder().withComponent(new IdentifierComponent(variantIdentifier)).build());
		binary = registry.register(factory.create(BinaryIdentifier.of(variantIdentifier, "tota"))).as(SharedLibraryBinary.class).get();

		binary.getLinkTask().configure(task -> ((LinkSharedLibraryTask) task).getTargetPlatform().set(create(of("macos-x64"))));
		subject = (LinkSharedLibraryTask) binary.getLinkTask().get();
	}

	@Test
	void usesBinaryNameAsTaskNameVariant() {
		assertThat(subject, named("linkQameSopuTota"));
	}

	@Test
	void hasLinkerArgs() {
		assertThat("not null as per contract", subject.getLinkerArgs(), notNullValue(ListProperty.class));
		assertThat("there should be a value", subject.getLinkerArgs(), presentProvider());
	}

	@Test
	void hasDescription() {
		assertThat(subject, TaskMatchers.description("Links the binary ':qame:sopu:tota'."));
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
		assertThat(subject.getLinkedFile(), providerOf(aFileBaseNamed(endsWith("da-bo"))));
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
	void hasNoLinkerArgumentsByDefault() {
		assertThat(subject.getLinkerArgs(), providerOf(emptyIterable()));
	}

	@Test
	void includesBinaryNameInDestinationDirectory() {
		assertThat(subject.getDestinationDirectory(), providerOf(aFileNamed("tota")));
	}

	@Test
	@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
	void hasImportLibraryOnWindows() {
		subject.getTargetPlatform().set(windowsPlatform());
		assertThat(subject.getImportLibrary(), presentProvider());
	}

	@Test
	@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
	void usesDestinationDirectoryAsImportLibraryFileParentDirectory() {
		subject.getTargetPlatform().set(windowsPlatform());
		val newDestinationDirectory = project.file("some-new-destination-directory");
		subject.getDestinationDirectory().set(newDestinationDirectory);
		assertThat(subject.getImportLibrary(), providerOf(aFile(parentFile(is(newDestinationDirectory)))));
	}

	@Test
	@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
	void doesNotHaveImportLibraryOnNonWindows() {
		subject.getTargetPlatform().set(nixPlatform());
		assertThat(subject.getImportLibrary(), absentProvider());
	}

	@Test
	@EnabledOnOs(OS.MAC)
	@PluginRequirement.Require(type = SwiftCompilerPlugin.class) // only for Swiftc, at the moment
	void addsMacOsSdkPathToLinkerArguments() {
		subject.getTargetPlatform().set(macosPlatform());
		assertThat(subject.getLinkerArgs(), providerOf(hasItem("-sdk")));
	}


	private Configuration linkLibraries() {
		return project.getConfigurations().getByName("qameSopuTotaLinkLibraries");
	}

	@Test
	void attachesLinkLibrariesToLinkTaskLibraries() {
		// We mock the resolution process by forcing a file dependency on the configuration
		linkLibraries().getDependencies().add(createDependency(project.files().from("libfoo.a")));
		assertThat(subject.getLibs(), contains(aFile(withAbsolutePath(endsWith("libfoo.a")))));
	}

	@Test
	void linksLinkLibrariesConfigurationToLinkTaskAsFrameworkLinkArguments() throws IOException {
		val artifact = Files.createTempDirectory("Vufa.framework").toFile();
		val frameworkProducer = ProjectTestUtils.createChildProject(project);
		frameworkProducer.getConfigurations().create("linkElements",
			configureAsConsumable()
				.andThen(configureAttributes(forUsage(project.getObjects().named(Usage.class, Usage.NATIVE_LINK))))
				.andThen(configureAttributes(it -> it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
					project.getObjects().named(LibraryElements.class, "framework-bundle"))))
				.andThen(it -> it.getOutgoing().artifact(artifact, t -> t.setType("framework")))
		);

		linkLibraries().getDependencies().add(createDependency(frameworkProducer));
		assertThat(subject.getLibs(), not(hasItem(aFile(artifact))));
		assertThat(subject.getLinkerArgs(), providerOf(containsInRelativeOrder(
			"-F", artifact.getParentFile().getAbsolutePath(), "-framework", "Vufa"
		)));
	}
}
