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
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.javadoc.Javadoc;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.StreamSupport.stream;

/**
 * Specifies the search paths for finding source files when passing package names or the {@literal -subpackages} option into the {@literal javadoc} command.
 */
public final class JavadocSourcePathsOption implements Action<Javadoc> {
	private static final String SOURCE_PATHS_EXTENSION_NAME = "sourcePaths";
	private final Project project;

	JavadocSourcePathsOption(Project project) {
		this.project = project;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public void execute(Javadoc task) {
		final ConfigurableFileCollection sourcePaths = project.getObjects().fileCollection();
		task.getExtensions().add(ConfigurableFileCollection.class, SOURCE_PATHS_EXTENSION_NAME, sourcePaths);

		final ListProperty<String> args = new JavadocPropertyFactory(project).named(SOURCE_PATHS_EXTENSION_NAME + "Args").apply(task);
		args.value(sourcePaths.getElements().map(asSourcePathFlags()).orElse(emptyList())).disallowChanges();
	}

	private static Transformer<Iterable<String>, Collection<FileSystemLocation>> asSourcePathFlags() {
		return values -> {
			if (values.isEmpty()) {
				return Collections.emptyList();
			} else {
				return asList("-sourcepath", stream(values.spliterator(), false).map(it -> it.getAsFile().getAbsolutePath()).collect(Collectors.joining(File.pathSeparator)));
			}
		};
	}

	public static ConfigurableFileCollection sourcePaths(Javadoc self) {
		return (ConfigurableFileCollection) self.getExtensions().getByName(SOURCE_PATHS_EXTENSION_NAME);
	}
}
