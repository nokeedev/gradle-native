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
 * Generates documentation from source files in the specified packages and recursively in their subpackages.
 * This option is useful when adding new subpackages to the source code because they're automatically included.
 * Each package argument is any top-level subpackage (such as {@literal java}) or fully qualified package (such as {@literal javax.swing}) that doesn't need to contain source files.
 * Arguments are separated by colons on all operating systems.
 * Wild cards aren't allowed.
 * Use {@literal -sourcepath} to specify where to find the packages.
 * This option doesn't process source files that are in the source tree but don't belong to the packages.
 */
public final class JavadocSubpackagesOption implements Action<Javadoc> {
	private static final String SUBPACKAGES_EXTENSION_NAME = "subpackages";
	private static final TypeOf<SetProperty<String>> SUBPACKAGES_EXTENSION_TYPE = new TypeOf<SetProperty<String>>() {};
	private final Project project;

	JavadocSubpackagesOption(Project project) {
		this.project = project;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public void execute(Javadoc task) {
		final SetProperty<String> subpackages = project.getObjects().setProperty(String.class);
		task.getExtensions().add(SUBPACKAGES_EXTENSION_TYPE, SUBPACKAGES_EXTENSION_NAME, subpackages);

		final ListProperty<String> args = new JavadocPropertyFactory(project).named(SUBPACKAGES_EXTENSION_NAME + "Args").apply(task);
		args.value(subpackages.map(asSubpackagesFlags()).orElse(emptyList())).disallowChanges();
	}

	private static Transformer<Iterable<String>, Collection<String>> asSubpackagesFlags() {
		return values -> {
			if (values.isEmpty()) {
				return Collections.emptyList();
			} else {
				return Arrays.asList("-subpackages", join(File.pathSeparator, values));
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static SetProperty<String> subpackages(Javadoc self) {
		return (SetProperty<String>) self.getExtensions().getByName(SUBPACKAGES_EXTENSION_NAME);
	}
}
