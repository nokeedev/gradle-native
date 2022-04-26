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
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.nativebase.HasObjectFiles;
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
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.platform.base.ToolChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import static dev.nokee.internal.testing.FileSystemMatchers.aFileNamed;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

@PluginRequirement.Require(type = NativeComponentBasePlugin.class)
@SuppressWarnings("unchecked")
class StaticLibraryBinarySpecCreateTaskObjectFilesIntegrationTest extends AbstractPluginTest {
	StaticLibraryBinary binary;
	CreateStaticLibraryTask subject;

	@BeforeEach
	void createSubject() {
		val factory = project.getExtensions().getByType(StaticLibraryBinaryRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val projectIdentifier = ProjectIdentifier.of(project);
		binary = registry.register(factory.create(BinaryIdentifier.of(projectIdentifier, "liku"))).as(StaticLibraryBinary.class).get();

		binary.getCreateTask().configure(task -> ((CreateStaticLibraryTask) task).getTargetPlatform().set(create(of("macos-x64"))));
		subject = (CreateStaticLibraryTask) binary.getCreateTask().get();
	}

	@Test
	void hasNoSourcesByDefault() {
		assertThat(subject.getSource(), emptyIterable());
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
		val compileTasks = ModelProperties.getProperty(binary, "compileTasks");
		((MapProperty<String, Object>) ModelNodes.of(compileTasks).get(GradlePropertyComponent.class).get()).put("suti", compileTask);

		assertThat(subject.getSource(), contains(aFileNamed("foo.o"), aFileNamed("foo.obj")));
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
		val compileTasks = ModelProperties.getProperty(binary, "compileTasks");
		((MapProperty<String, Object>) ModelNodes.of(compileTasks).get(GradlePropertyComponent.class).get()).put("kedi", compileTask);

		assertThat(subject.getSource(), contains(aFileNamed("bar.o"), aFileNamed("bar.obj")));
	}

	@Test
	void doesNotThrowExceptionWhenResolvingSourcesWithCompileTasksWithoutObjectFiles() {
		val compileTask = project().getTasks().register("xuvi", MySourceCompileTask.class);
		val compileTasks = ModelProperties.getProperty(binary, "compileTasks");
		((MapProperty<String, Object>) ModelNodes.of(compileTasks).get(GradlePropertyComponent.class).get()).put("xuvi", compileTask);
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
