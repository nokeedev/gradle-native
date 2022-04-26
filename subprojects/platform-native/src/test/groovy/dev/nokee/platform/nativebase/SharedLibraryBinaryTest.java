/*
 * Copyright 2021 the original author or authors.
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
import dev.nokee.internal.testing.NativeServicesInitializedOnWindows;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.nativebase.HasObjectFiles;
import dev.nokee.language.nativebase.HeaderSearchPath;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.platform.nativebase.testers.SharedLibraryBinaryIntegrationTester;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;
import org.gradle.platform.base.ToolChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.aFileNamed;
import static dev.nokee.internal.testing.FileSystemMatchers.parentFile;
import static dev.nokee.internal.testing.GradleProviderMatchers.absentProvider;
import static dev.nokee.internal.testing.GradleProviderMatchers.presentProvider;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@PluginRequirement.Require(type = NativeComponentBasePlugin.class)
class SharedLibraryBinaryTest extends AbstractPluginTest {
	private SharedLibraryBinary subject;

	@BeforeEach
	void createSubject() {
		val factory = project.getExtensions().getByType(SharedLibraryBinaryRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val projectIdentifier = ProjectIdentifier.of(project);
		val componentIdentifier = ComponentIdentifier.of("nuli", projectIdentifier);
		registry.register(ModelRegistration.builder().withComponent(new IdentifierComponent(componentIdentifier)).build());
		val variantIdentifier = VariantIdentifier.of("cuzu", Variant.class, componentIdentifier);
		registry.register(ModelRegistration.builder().withComponent(new IdentifierComponent(variantIdentifier)).build());
		subject = registry.register(factory.create(BinaryIdentifier.of(variantIdentifier, "ruca"))).as(SharedLibraryBinary.class).get();
	}

	@Nested
	class BinaryTest extends SharedLibraryBinaryIntegrationTester {
		@BeforeEach
		public void configureTargetPlatform() {
			((LinkSharedLibraryTask) project.getTasks().getByName("link" + capitalize(variantName()))).getTargetPlatform()
				.set(create(of("macos-x64")));
		}

		@Override
		public SharedLibraryBinary subject() {
			return subject;
		}

		@Override
		public Project project() {
			return project;
		}

		@Override
		public String variantName() {
			return "nuliCuzuRuca";
		}

		@Override
		public String displayName() {
			return "binary ':nuli:cuzu:ruca'";
		}

		private SharedLibraryBinary binary() {
			return subject;
		}

		private LinkSharedLibraryTask linkTask() {
			return (LinkSharedLibraryTask) project.getTasks().getByName("link" + capitalize(variantName()));
		}

		@Test
		void noCompileTasksByDefault() {
			assertThat(subject().getCompileTasks().get(), emptyIterable());
		}

		@Test
		void usesBinaryNameAsBaseNameByDefault() {
			subject().getBaseName().set((String) null); // force convention
			assertThat(subject().getBaseName(), providerOf("ruca"));
		}

		@Nested
		class LinkSharedLibraryTaskTest {
			public LinkSharedLibraryTask subject() {
				return (LinkSharedLibraryTask) project().getTasks().getByName("link" + capitalize(variantName()));
			}

			@Test
			void isNotDebuggableByDefault() {
				assertThat(subject().isDebuggable(), is(false));
			}

			@Test
			void hasNoLinkerArgumentsByDefault() {
				assertThat(subject().getLinkerArgs(), providerOf(emptyIterable()));
			}

			@Test
			void hasNoSourcesByDefault() {
				assertThat(subject().getSource(), emptyIterable());
			}

			@Test
			void includesBinaryNameInDestinationDirectory() {
				assertThat(subject().getDestinationDirectory(), providerOf(aFileNamed("ruca")));
			}

			@Test
			void includesNativeSourceCompileTaskAsLinkTaskSources() throws IOException {
				val compileTask = project().getTasks().register("suti", MyNativeSourceCompileTask.class, task -> {
					try {
						task.getObjectFiles().from(createFile(project().getLayout().getProjectDirectory().file("foo.o")));
						task.getObjectFiles().from(createFile(project().getLayout().getProjectDirectory().file("foo.obj")));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
				val compileTasks = ModelProperties.getProperty(binary(), "compileTasks");
				((MapProperty<String, Object>) ModelNodes.of(compileTasks).get(GradlePropertyComponent.class).get()).put("suti", compileTask);

				assertThat(subject().getSource(), contains(aFileNamed("foo.o"), aFileNamed("foo.obj")));
			}

			@Test
			void includesSourceCompileTaskWithObjectFilesAsLinkTaskSources() throws IOException {
				val compileTask = project().getTasks().register("kedi", MySourceCompileWithObjectFilesTask.class, task -> {
					try {
						task.getObjectFiles().from(createFile(project().getLayout().getProjectDirectory().file("bar.o")));
						task.getObjectFiles().from(createFile(project().getLayout().getProjectDirectory().file("bar.obj")));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
				val compileTasks = ModelProperties.getProperty(binary(), "compileTasks");
				((MapProperty<String, Object>) ModelNodes.of(compileTasks).get(GradlePropertyComponent.class).get()).put("kedi", compileTask);

				assertThat(subject().getSource(), contains(aFileNamed("bar.o"), aFileNamed("bar.obj")));
			}

			@Test
			void doesNotThrowExceptionWhenResolvingSourcesWithCompileTasksWithoutObjectFiles() {
				val compileTask = project().getTasks().register("xuvi", MySourceCompileTask.class);
				val compileTasks = ModelProperties.getProperty(binary(), "compileTasks");
				((MapProperty<String, Object>) ModelNodes.of(compileTasks).get(GradlePropertyComponent.class).get()).put("xuvi", compileTask);
				assertThat(subject().getSource(), emptyIterable());
			}

			@Test
			@NativeServicesInitializedOnWindows
			@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
			void hasImportLibraryOnWindows() {
				subject().getTargetPlatform().set(windowsPlatform());
				assertThat(subject().getImportLibrary(), presentProvider());
			}

			@Test
			@NativeServicesInitializedOnWindows
			@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
			void usesDestinationDirectoryAsImportLibraryFileParentDirectory() {
				subject().getTargetPlatform().set(windowsPlatform());
				val newDestinationDirectory = project().file("some-new-destination-directory");
				subject().getDestinationDirectory().set(newDestinationDirectory);
				assertThat(subject().getImportLibrary(), providerOf(aFile(parentFile(is(newDestinationDirectory)))));
			}

			@Test
			@NativeServicesInitializedOnWindows
			@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
			void doesNotHaveImportLibraryOnNonWindows() {
				subject().getTargetPlatform().set(nixPlatform());
				assertThat(subject().getImportLibrary(), absentProvider());
			}

			@Test
			@EnabledOnOs(OS.MAC)
			@PluginRequirement.Require(type = SwiftCompilerPlugin.class) // only for Swiftc, at the moment
			void addsMacOsSdkPathToLinkerArguments() {
				subject().getTargetPlatform().set(macosPlatform());
				assertThat(subject().getLinkerArgs(), providerOf(hasItem("-sdk")));
			}
		}
	}

	private static File createFile(RegularFile provider) throws IOException {
		val path = provider.getAsFile();
		path.getParentFile().mkdirs();
		path.createNewFile();
		return path;
	}

	public static abstract class MySourceCompileTask extends DefaultTask implements SourceCompile {
		@Override
		public abstract Property<ToolChain> getToolChain();
	}

	public static abstract class MyNativeSourceCompileTask extends DefaultTask implements NativeSourceCompile {
		@Override
		public abstract Property<NativeToolChain> getToolChain();

		@Override
		public abstract Property<Set<HeaderSearchPath>> getHeaderSearchPaths();
	}

	public static abstract class MySourceCompileWithObjectFilesTask extends DefaultTask implements SourceCompile, HasObjectFiles {
		@Override
		public abstract Property<ToolChain> getToolChain();
	}

	private static NativePlatform windowsPlatform() {
		return create(of("windows-x86"));
	}

	private static NativePlatform nixPlatform() {
		return create(of("linux-x86"));
	}

	private static NativePlatform macosPlatform() {
		return create(of("osx-x64"));
	}
}
