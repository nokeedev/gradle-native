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
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.nativebase.HasObjectFiles;
import dev.nokee.language.nativebase.HeaderSearchPath;
import dev.nokee.language.nativebase.internal.DefaultNativeToolChainSelector;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.IsModelProperty;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.type.ModelType;
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
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.provider.Property;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;
import org.gradle.platform.base.ToolChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static dev.nokee.internal.testing.FileSystemMatchers.*;
import static dev.nokee.internal.testing.GradleProviderMatchers.*;
import static dev.nokee.internal.testing.ProjectMatchers.buildDependencies;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.host;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@PluginRequirement.Require(type = NativeComponentBasePlugin.class)
class SharedLibraryBinaryTest extends AbstractPluginTest {
	private SharedLibraryBinary subject;

	@BeforeEach
	void createSubject() {
		val factory = project.getExtensions().getByType(SharedLibraryBinaryRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val projectIdentifier = ProjectIdentifier.of(project);
		val componentIdentifier = ComponentIdentifier.of("nuli", projectIdentifier);
		registry.register(ModelRegistration.builder().withComponent(toPath(componentIdentifier)).build());
		val variantIdentifier = VariantIdentifier.of("cuzu", Variant.class, componentIdentifier);
		registry.register(ModelRegistration.builder().withComponent(toPath(variantIdentifier)).build());
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
		class BuildableTest {
			@Test
			void isBuildableIfLinkTaskBuildable() {
				project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);
				linkTask().getTargetPlatform().set(create(host()));
				assertThat(subject().isBuildable(), is(true));
			}

			@Test
			void isNotBuildableIfLinkTaskNotBuildable() {
				project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);
				linkTask().getTargetPlatform().set(create(of("unknown-unknown")));
				assertThat(subject().isBuildable(), is(false));
			}

			@Test
			void isBuildableIfAllCompileTasksAreBuildable() {
				project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);
				val toolChainSelector = new DefaultNativeToolChainSelector(((ProjectInternal) project).getModelRegistry(), project.getProviders());

				linkTask().getTargetPlatform().set(create(host()));

				val compileTask = project().getTasks().create("tovi", CCompileTask.class);
				compileTask.getTargetPlatform().set(create(host()));
				compileTask.getToolChain().set(toolChainSelector.select(compileTask));
				val compileTasks = ModelProperties.getProperty(binary(), "compileTasks");
				val newPropertyIdentifier = ModelPropertyIdentifier.of(ModelNodes.of(compileTasks).getComponent(DomainObjectIdentifier.class), "tovi");
				project.getExtensions().getByType(ModelRegistry.class).register(ModelRegistration.builder()
					.withComponent(newPropertyIdentifier)
					.withComponent(toPath(newPropertyIdentifier))
					.withComponent(IsModelProperty.tag())
					.withComponent(createdUsing(ModelType.of(SourceCompile.class), () -> compileTask))
					.build());

				assertThat(subject().isBuildable(), is(true));
			}

			@Test
			void isBuildableIfSwiftCompileTasksAreBuildable() {
				project.getPluginManager().apply(SwiftCompilerPlugin.class);
				val toolChainSelector = new DefaultNativeToolChainSelector(((ProjectInternal) project).getModelRegistry(), project.getProviders());

				linkTask().getTargetPlatform().set(create(host()));

				val compileTask = project().getTasks().create("vavu", SwiftCompileTask.class);
				compileTask.getTargetPlatform().set(create(host()));
				compileTask.getToolChain().set(toolChainSelector.select(compileTask));
				val compileTasks = ModelProperties.getProperty(binary(), "compileTasks");
				val newPropertyIdentifier = ModelPropertyIdentifier.of(ModelNodes.of(compileTasks).getComponent(DomainObjectIdentifier.class), "vavu");
				project.getExtensions().getByType(ModelRegistry.class).register(ModelRegistration.builder()
					.withComponent(newPropertyIdentifier)
					.withComponent(toPath(newPropertyIdentifier))
					.withComponent(IsModelProperty.tag())
					.withComponent(createdUsing(ModelType.of(SourceCompile.class), () -> compileTask))
					.build());

				assertThat(subject().isBuildable(), is(true));
			}

			@Test
			void isNotBuildableIfAnyCompileTasksAreNotBuildable() {
				project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);
				val toolChainSelector = new DefaultNativeToolChainSelector(((ProjectInternal) project).getModelRegistry(), project.getProviders());
				project.getExtensions().getByType(NativeToolChainRegistry.class).withType(AbstractGccCompatibleToolChain.class, toolchain -> {
					// Ensure toolchain is known but not buildable
					toolchain.target("notbuildable", it -> {
						it.getLinker().setExecutable("not-found");
						it.getAssembler().setExecutable("not-found");
						it.getcCompiler().setExecutable("not-found");
						it.getCppCompiler().setExecutable("not-found");
						it.getObjcCompiler().setExecutable("not-found");
						it.getObjcppCompiler().setExecutable("not-found");
						it.getStaticLibArchiver().setExecutable("not-found");
					});
				});

				linkTask().getTargetPlatform().set(create(host()));

				val compileTask = project().getTasks().create("qizo", CCompileTask.class);
				compileTask.getTargetPlatform().set(create(of("not-buildable")));
				compileTask.getToolChain().set(toolChainSelector.select(compileTask));

				val compileTasks = ModelProperties.getProperty(binary(), "compileTasks");
				val newPropertyIdentifier = ModelPropertyIdentifier.of(ModelNodes.of(compileTasks).getComponent(DomainObjectIdentifier.class), "qizo");
				project.getExtensions().getByType(ModelRegistry.class).register(ModelRegistration.builder()
					.withComponent(newPropertyIdentifier)
					.withComponent(toPath(newPropertyIdentifier))
					.withComponent(IsModelProperty.tag())
					.withComponent(createdUsing(ModelType.of(SourceCompile.class), () -> compileTask))
					.build());

				assertThat(subject().isBuildable(), is(false));
			}
		}

		@Test
		void includesAllCompileTasksAsBuildDependencies() {
			val compileTask = project().getTasks().create("xuvi", MySourceCompileTask.class);
			val compileTasks = ModelProperties.getProperty(subject(), "compileTasks");
			val newPropertyIdentifier = ModelPropertyIdentifier.of(ModelNodes.of(compileTasks).getComponent(DomainObjectIdentifier.class), "xuvi");
			project.getExtensions().getByType(ModelRegistry.class).register(ModelRegistration.builder()
				.withComponent(newPropertyIdentifier)
				.withComponent(toPath(newPropertyIdentifier))
				.withComponent(IsModelProperty.tag())
				.withComponent(createdUsing(ModelType.of(SourceCompile.class), () -> compileTask))
				.build());
			assertThat(subject(), buildDependencies(hasItem(compileTask)));
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
				val compileTask = project().getTasks().create("suti", MyNativeSourceCompileTask.class);
				compileTask.getObjectFiles().from(createFile(project().getLayout().getProjectDirectory().file("foo.o")));
				compileTask.getObjectFiles().from(createFile(project().getLayout().getProjectDirectory().file("foo.obj")));
				val compileTasks = ModelProperties.getProperty(binary(), "compileTasks");
				val newPropertyIdentifier = ModelPropertyIdentifier.of(ModelNodes.of(compileTasks).getComponent(DomainObjectIdentifier.class), "suti");
				project.getExtensions().getByType(ModelRegistry.class).register(ModelRegistration.builder()
					.withComponent(newPropertyIdentifier)
					.withComponent(toPath(newPropertyIdentifier))
					.withComponent(IsModelProperty.tag())
					.withComponent(createdUsing(ModelType.of(NativeSourceCompile.class), () -> compileTask))
					.build());
				assertThat(subject().getSource(), contains(aFileNamed("foo.o"), aFileNamed("foo.obj")));
			}

			@Test
			void includesSourceCompileTaskWithObjectFilesAsLinkTaskSources() throws IOException {
				val compileTask = project().getTasks().create("kedi", MySourceCompileWithObjectFilesTask.class);
				compileTask.getObjectFiles().from(createFile(project().getLayout().getProjectDirectory().file("bar.o")));
				compileTask.getObjectFiles().from(createFile(project().getLayout().getProjectDirectory().file("bar.obj")));
				val compileTasks = ModelProperties.getProperty(binary(), "compileTasks");
				val newPropertyIdentifier = ModelPropertyIdentifier.of(ModelNodes.of(compileTasks).getComponent(DomainObjectIdentifier.class), "kedi");
				project.getExtensions().getByType(ModelRegistry.class).register(ModelRegistration.builder()
					.withComponent(newPropertyIdentifier)
					.withComponent(toPath(newPropertyIdentifier))
					.withComponent(IsModelProperty.tag())
					.withComponent(createdUsing(ModelType.of(SourceCompile.class), () -> compileTask))
					.build());
				assertThat(subject().getSource(), contains(aFileNamed("bar.o"), aFileNamed("bar.obj")));
			}

			@Test
			void doesNotThrowExceptionWhenResolvingSourcesWithCompileTasksWithoutObjectFiles() {
				val compileTask = project().getTasks().create("xuvi", MySourceCompileTask.class);
				val compileTasks = ModelProperties.getProperty(binary(), "compileTasks");
				val newPropertyIdentifier = ModelPropertyIdentifier.of(ModelNodes.of(compileTasks).getComponent(DomainObjectIdentifier.class), "xuvi");
				project.getExtensions().getByType(ModelRegistry.class).register(ModelRegistration.builder()
					.withComponent(newPropertyIdentifier)
					.withComponent(toPath(newPropertyIdentifier))
					.withComponent(IsModelProperty.tag())
					.withComponent(createdUsing(ModelType.of(SourceCompile.class), () -> compileTask))
					.build());
				assertThat(subject().getSource(), emptyIterable());
			}

			@Test
			void hasImportLibraryOnWindows() {
				project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);
				subject().getTargetPlatform().set(windowsPlatform());
				assertThat(subject().getImportLibrary(), presentProvider());
			}

			@Test
			void usesDestinationDirectoryAsImportLibraryFileParentDirectory() {
				project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);
				subject().getTargetPlatform().set(windowsPlatform());
				val newDestinationDirectory = project().file("some-new-destination-directory");
				subject().getDestinationDirectory().set(newDestinationDirectory);
				assertThat(subject().getImportLibrary(), providerOf(aFile(parentFile(is(newDestinationDirectory)))));
			}

			@Test
			void doesNotHaveImportLibraryOnNonWindows() {
				project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);
				subject().getTargetPlatform().set(nixPlatform());
				assertThat(subject().getImportLibrary(), absentProvider());
			}

			@Test
			@EnabledOnOs(OS.MAC)
			void addsMacOsSdkPathToLinkerArguments() {
				project().getPluginManager().apply(SwiftCompilerPlugin.class); // only for Swiftc, at the moment
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
