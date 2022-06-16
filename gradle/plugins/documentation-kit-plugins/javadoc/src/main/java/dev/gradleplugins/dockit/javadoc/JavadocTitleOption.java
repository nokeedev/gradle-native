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
import org.gradle.api.provider.Property;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.javadoc.Javadoc;

import java.util.Arrays;

import static java.util.Collections.emptyList;

public final class JavadocTitleOption implements Action<Javadoc> {
	private static final String TITLE_EXTENSION_NAME = "title";
	private static final TypeOf<Property<String>> TITLE_EXTENSION_TYPE = new TypeOf<Property<String>>() {};
	private final Project project;

	JavadocTitleOption(Project project) {
		this.project = project;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public void execute(Javadoc task) {
		final Property<String> title = project.getObjects().property(String.class);
		task.getExtensions().add(TITLE_EXTENSION_TYPE, TITLE_EXTENSION_NAME, title);

		final ListProperty<String> args = new JavadocPropertyFactory(project).named(TITLE_EXTENSION_NAME + "Args").apply(task);
		args.addAll(title.map(asDocTitleFlags()).orElse(emptyList()));
		args.addAll(title.map(asWindowTitleFlags()).orElse(emptyList()));
		args.disallowChanges();
	}

	private static Transformer<Iterable<String>, String> asDocTitleFlags() {
		return value -> Arrays.asList("-doctitle", quote(value));
	}

	private static Transformer<Iterable<String>, String> asWindowTitleFlags() {
		return value -> Arrays.asList("-windowtitle", quote(value));
	}

	private static String quote(String stringToQuote) {
		return "\"" + stringToQuote + "\"";
	}

	@SuppressWarnings("unchecked")
	public static Property<String> title(Javadoc self) {
		return (Property<String>) self.getExtensions().getByName(TITLE_EXTENSION_NAME);
	}
}
