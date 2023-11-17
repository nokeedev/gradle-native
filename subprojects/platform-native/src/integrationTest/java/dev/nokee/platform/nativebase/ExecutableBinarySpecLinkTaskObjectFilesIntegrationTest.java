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
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.nativebase.HasObjectFiles;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Property;
import org.gradle.platform.base.ToolChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import static dev.nokee.internal.testing.FileSystemMatchers.aFileNamed;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.artifacts;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

@PluginRequirement.Require(type = NativeComponentBasePlugin.class)
@IntegrationTest
@SuppressWarnings("unchecked")
class ExecutableBinarySpecLinkTaskObjectFilesIntegrationTest {
	@GradleProject Project project;
	ExecutableBinaryInternal binary;
	LinkExecutableTask subject;

	@BeforeEach
	void createSubject() {
		binary = artifacts(project).register("liku", ExecutableBinaryInternal.class).get();

		binary.getLinkTask().configure(task -> ((LinkExecutableTask) task).getTargetPlatform().set(create(of("macos-x64"))));
		subject = (LinkExecutableTask) binary.getLinkTask().get();
	}

	@Test
	void hasNoSourcesByDefault() {
		assertThat(subject.getSource(), emptyIterable());
	}

	@Test
	void includesNativeSourceCompileTaskAsLinkTaskSources() throws IOException {
		model(project, registryOf(Task.class)).register(binary.getIdentifier().child("suti"), MyNativeSourceCompileTask.class).configure(task -> {
			try {
				task.getObjectFiles().from(createFile(project.getLayout().getProjectDirectory().file("foo.o")));
				task.getObjectFiles().from(createFile(project.getLayout().getProjectDirectory().file("foo.obj")));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});

		assertThat(subject.getSource(), contains(aFileNamed("foo.o"), aFileNamed("foo.obj")));
	}

	@Test
	void includesSourceCompileTaskWithObjectFilesAsLinkTaskSources() throws IOException {
		model(project, registryOf(Task.class)).register(binary.getIdentifier().child("kedi"), MySourceCompileWithObjectFilesTask.class).configure(task -> {
			try {
				task.getObjectFiles().from(createFile(project.getLayout().getProjectDirectory().file("bar.o")));
				task.getObjectFiles().from(createFile(project.getLayout().getProjectDirectory().file("bar.obj")));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});

		assertThat(subject.getSource(), contains(aFileNamed("bar.o"), aFileNamed("bar.obj")));
	}

	@Test
	void doesNotThrowExceptionWhenResolvingSourcesWithCompileTasksWithoutObjectFiles() {
		model(project, registryOf(Task.class)).register(binary.getIdentifier().child("xuvi"), MySourceCompileTask.class);
		assertThat(subject.getSource(), emptyIterable());
	}

	private static File createFile(RegularFile provider) throws IOException {
		val path = provider.getAsFile();
		path.getParentFile().mkdirs();
		path.createNewFile();
		return path;
	}

	public static abstract class MySourceCompileWithObjectFilesTask extends DefaultTask implements SourceCompile, HasObjectFiles {
		@Override
		public abstract Property<ToolChain> getToolChain();
	}
}
