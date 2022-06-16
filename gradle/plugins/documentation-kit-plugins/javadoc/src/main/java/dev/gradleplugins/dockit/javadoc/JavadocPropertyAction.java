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
package dev.gradleplugins.dockit.javadoc;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.javadoc.Javadoc;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

final class JavadocPropertyAction {
	private static final TypeOf<ListProperty<String>> ADDITIONAL_ARGS_EXTENSION_TYPE = new TypeOf<ListProperty<String>>() {};
	private final ObjectFactory objects;
	private final String name;

	public JavadocPropertyAction(ObjectFactory objects, String name) {
		this.objects = objects;
		this.name = name;
	}

	@SuppressWarnings("UnstableApiUsage")
	public ListProperty<String> apply(Javadoc task) {
		// To delay the realization while keeping task dependencies, we _fake_ an input task property.
		final ListProperty<String> args = objects.listProperty(String.class);
		args.finalizeValueOnRead();

		// We register the property as extension for debugging purpose.
		task.getExtensions().add(ADDITIONAL_ARGS_EXTENSION_TYPE, name, args);

		// We register the property as a task input allowing task dependencies to be kept.
		task.getInputs().property(name, args);

		// Finally, before the task executes, we write the additional arguments to an option file.
		task.doFirst(new TaskAction<>(new PrepareAdditionalArgsOptionFile()));

		return args;
	}

	private static final class TaskAction<T extends Task> implements Action<Task> {
		private final Action<? super T> delegate;

		public TaskAction(Action<? super T> delegate) {
			this.delegate = delegate;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void execute(Task task) {
			delegate.execute((T) task);
		}
	}

	private final class PrepareAdditionalArgsOptionFile implements Action<Javadoc> {
		@Override
		public void execute(Javadoc task) {
			if (!additionalArgs(task).isEmpty()) {
				task.getOptions().optionFiles(writeAdditionalArgsOptionFile(task));
			}
		}

		private File writeAdditionalArgsOptionFile(Javadoc self) {
			try {
				return Files.write(optionFilePath(self), additionalArgs(self), UTF_8, CREATE, TRUNCATE_EXISTING).toFile();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		@SuppressWarnings("unchecked")
		private List<String> additionalArgs(Javadoc self) {
			return ((Provider<List<String>>) self.getExtensions().getByName(name)).get();
		}

		private Path optionFilePath(Javadoc self) {
			return self.getTemporaryDir().toPath().resolve(name + ".options");
		}
	}
}
