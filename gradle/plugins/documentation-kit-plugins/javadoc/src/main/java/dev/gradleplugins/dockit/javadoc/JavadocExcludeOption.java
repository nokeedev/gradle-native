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
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.javadoc.Javadoc;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static java.lang.String.join;
import static java.util.Collections.emptyList;

/**
 * Unconditionally, excludes the specified packages and their subpackages from the list formed by {@literal -subpackages}.
 * It excludes those packages even when they would otherwise be included by some earlier or later {@literal -subpackages} option.
 */
public final class JavadocExcludeOption implements Action<Javadoc> {
	private static final String EXCLUDE_EXTENSION_NAME = "exclude";
	private static final TypeOf<SetProperty<String>> EXCLUDE_EXTENSION_TYPE = new TypeOf<SetProperty<String>>() {};
	private final Project project;

	JavadocExcludeOption(Project project) {
		this.project = project;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public void execute(Javadoc task) {
		final SetProperty<String> exclude = project.getObjects().setProperty(String.class);
		task.getExtensions().add(EXCLUDE_EXTENSION_TYPE, EXCLUDE_EXTENSION_NAME, exclude);

		final ListProperty<String> args = new JavadocPropertyFactory(project).named(EXCLUDE_EXTENSION_NAME + "Args").apply(task);
		args.value(exclude.map(asExcludeFlags()).orElse(emptyList())).disallowChanges();
	}

	private static Transformer<Iterable<String>, Collection<String>> asExcludeFlags() {
		return values -> {
			if (values.isEmpty()) {
				return Collections.emptyList();
			} else {
				return Arrays.asList("-exclude", join(File.pathSeparator, values));
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static SetProperty<String> exclude(Javadoc self) {
		return (SetProperty<String>) self.getExtensions().getByName(EXCLUDE_EXTENSION_NAME);
	}
}
