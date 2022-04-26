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
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.nativebase.internal.DefaultNativeToolChainSelector;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.nativebase.internal.StaticLibraryBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import lombok.val;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.provider.MapProperty;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static dev.nokee.internal.testing.ProjectMatchers.buildDependencies;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.host;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@PluginRequirement.Require(type = NativeComponentBasePlugin.class)
@SuppressWarnings("unchecked")
class StaticLibraryBinarySpecBuildabilityIntegrationTest extends AbstractPluginTest {
	StaticLibraryBinary subject;
	CreateStaticLibraryTask createTask;

	@BeforeEach
	void createSubject() {
		val factory = project.getExtensions().getByType(StaticLibraryBinaryRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val projectIdentifier = ProjectIdentifier.of(project);
		subject = registry.register(factory.create(BinaryIdentifier.of(projectIdentifier, "liku"))).as(StaticLibraryBinary.class).get();
		createTask = (CreateStaticLibraryTask) subject.getCreateTask().get();
	}

	@Test
	void includesCreateTaskAsBuildDependencies() {
		assertThat(subject, buildDependencies(hasItem(subject.getCreateTask().get())));
	}

	@Test
	@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
	void isBuildableIfCreateTaskBuildable() {
		createTask.getTargetPlatform().set(create(host()));
		assertThat(subject.isBuildable(), is(true));
	}

	@Test
	@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
	void isNotBuildableIfCreateTaskNotBuildable() {
		createTask.getTargetPlatform().set(create(of("unknown-unknown")));
		assertThat(subject.isBuildable(), is(false));
	}

	@Test
	@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
	void isBuildableIfAllCompileTasksAreBuildable() {
		val toolChainSelector = new DefaultNativeToolChainSelector(((ProjectInternal) project).getModelRegistry(), project.getProviders());

		createTask.getTargetPlatform().set(create(host()));

		val compileTask = project().getTasks().register("tovi", CCompileTask.class, task -> {
			task.getTargetPlatform().set(create(host()));
			task.getToolChain().set(toolChainSelector.select(task));
		});
		val compileTasks = ModelProperties.getProperty(subject, "compileTasks");
		((MapProperty<String, Object>) ModelNodes.of(compileTasks).get(GradlePropertyComponent.class).get()).put("tovi", compileTask);

		assertThat(subject.isBuildable(), is(true));
	}

	@Test
	@DisabledOnOs(OS.WINDOWS)
	@PluginRequirement.Require(type = SwiftCompilerPlugin.class)
	void isBuildableIfSwiftCompileTasksAreBuildable() {
		val toolChainSelector = new DefaultNativeToolChainSelector(((ProjectInternal) project).getModelRegistry(), project.getProviders());

		createTask.getTargetPlatform().set(create(host()));

		val compileTask = project().getTasks().register("vavu", SwiftCompileTask.class, task -> {
			task.getTargetPlatform().set(create(host()));
			task.getToolChain().set(toolChainSelector.select(task));
		});
		val compileTasks = ModelProperties.getProperty(subject, "compileTasks");
		((MapProperty<String, Object>) ModelNodes.of(compileTasks).get(GradlePropertyComponent.class).get()).put("vavu", compileTask);

		assertThat(subject.isBuildable(), is(true));
	}

	@Test
	@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
	void isNotBuildableIfAnyCompileTasksAreNotBuildable() {
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

		createTask.getTargetPlatform().set(create(host()));

		val compileTask = project().getTasks().create("qizo", CCompileTask.class);
		compileTask.getTargetPlatform().set(create(of("not-buildable")));
		compileTask.getToolChain().set(toolChainSelector.select(compileTask));

		val compileTasks = ModelProperties.getProperty(subject, "compileTasks");
		((MapProperty<String, Object>) ModelNodes.of(compileTasks).get(GradlePropertyComponent.class).get()).put("qizo", compileTask);

		assertThat(subject.isBuildable(), is(false));
	}

	@Test
	void includesAllCompileTasksAsBuildDependencies() {
		val compileTask = project().getTasks().register("xuvi", MySourceCompileTask.class);
		val compileTasks = ModelProperties.getProperty(subject, "compileTasks");
		((MapProperty<String, Object>) ModelNodes.of(compileTasks).get(GradlePropertyComponent.class).get()).put("xuvi", compileTask);

		assertThat(subject, buildDependencies(hasItem(compileTask.get())));
	}
}
