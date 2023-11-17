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
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.nativebase.internal.DefaultNativeToolChainSelector;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.ModelElement;
import dev.nokee.platform.nativebase.internal.HasObjectFilesToBinaryTask;
import lombok.val;
import org.gradle.api.Buildable;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static dev.nokee.internal.testing.ProjectMatchers.buildDependencies;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.host;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("unchecked")
@IntegrationTest
abstract class NativeBinaryBuildabilityIntegrationTester<T extends NativeBinary & Buildable & HasObjectFilesToBinaryTask & ModelElement> {
	private T subject;
	@GradleProject protected Project project;

	public abstract T createSubject();

	@BeforeEach
	void initializeSubject() {
		subject = createSubject();
	}

	@Test
	void includesCreateOrLinkTaskAsBuildDependencies() {
		assertThat(subject, buildDependencies(hasItem(subject.getCreateOrLinkTask().get())));
	}

	@Test
	@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
	void isBuildableIfCreateOrLinkTaskBuildable() {
		subject.getCreateOrLinkTask().configure(task -> task.getTargetPlatform().set(create(host())));
		assertThat(subject.isBuildable(), is(true));
	}

	@Test
	@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
	void isNotBuildableIfCreateOrLinkTaskNotBuildable() {
		subject.getCreateOrLinkTask().configure(task -> task.getTargetPlatform().set(create(of("unknown-unknown"))));
		assertThat(subject.isBuildable(), is(false));
	}

	@Test
	@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
	void isBuildableIfAllCompileTasksAreBuildable(Project project) {
		val toolChainSelector = new DefaultNativeToolChainSelector(((ProjectInternal) project).getModelRegistry(), project.getProviders());

		subject.getCreateOrLinkTask().configure(task -> task.getTargetPlatform().set(create(host())));

		model(project, registryOf(Task.class)).register(subject.getIdentifier().child("tovi"), CCompileTask.class).configure(task -> {
			task.getTargetPlatform().set(create(host()));
			task.getToolChain().set(toolChainSelector.select(task));
		});

		assertThat(subject.isBuildable(), is(true));
	}

	@Test
	@DisabledOnOs(OS.WINDOWS)
	@PluginRequirement.Require(type = SwiftCompilerPlugin.class)
	void isBuildableIfSwiftCompileTasksAreBuildable(Project project) {
		val toolChainSelector = new DefaultNativeToolChainSelector(((ProjectInternal) project).getModelRegistry(), project.getProviders());

		subject.getCreateOrLinkTask().configure(task -> task.getTargetPlatform().set(create(host())));

		model(project, registryOf(Task.class)).register(subject.getIdentifier().child("vavu"), SwiftCompileTask.class).configure(task -> {
			task.getTargetPlatform().set(create(host()));
			task.getToolChain().set(toolChainSelector.select(task));
		});

		assertThat(subject.isBuildable(), is(true));
	}

	@Test
	@PluginRequirement.Require(type = NokeeStandardToolChainsPlugin.class)
	void isNotBuildableIfAnyCompileTasksAreNotBuildable(Project project) {
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

		subject.getCreateOrLinkTask().configure(task -> task.getTargetPlatform().set(create(host())));

		val compileTask = model(project, registryOf(Task.class)).register(subject.getIdentifier().child("qizo"), CCompileTask.class).get();
		compileTask.getTargetPlatform().set(create(of("not-buildable")));
		compileTask.getToolChain().set(toolChainSelector.select(compileTask));

		assertThat(subject.isBuildable(), is(false));
	}

	@Test
	void includesAllCompileTasksAsBuildDependencies(Project project) {
		val compileTask = model(project, registryOf(Task.class)).register(subject.getIdentifier().child("xuvi"), MySourceCompileTask.class);

		assertThat(subject, buildDependencies(hasItem(compileTask.get())));
	}
}
