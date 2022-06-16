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

import java.net.URI;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class JavadocLinksOption implements Action<Javadoc> {
	private static final String LINKS_EXTENSION_NAME = "links";
	private static final TypeOf<SetProperty<URI>> LINKS_EXTENSION_TYPE = new TypeOf<SetProperty<URI>>() {};
	private final Project project;

	JavadocLinksOption(Project project) {
		this.project = project;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public void execute(Javadoc task) {
		final SetProperty<URI> links = project.getObjects().setProperty(URI.class);
		task.getExtensions().add(LINKS_EXTENSION_TYPE, LINKS_EXTENSION_NAME, links);

		final ListProperty<String> args = new JavadocPropertyFactory(project).named(LINKS_EXTENSION_NAME + "Args").apply(task);
		args.value(links.map(asLinkFlags()).orElse(emptyList())).disallowChanges();
	}

	private static Transformer<Iterable<String>, Iterable<URI>> asLinkFlags() {
		return values -> stream(values.spliterator(), false)
			.flatMap(it -> Stream.of("-link", it.toString()))
			.collect(toList());
	}

	@SuppressWarnings("unchecked")
	public static SetProperty<URI> links(Javadoc self) {
		return (SetProperty<URI>) self.getExtensions().getByName(LINKS_EXTENSION_NAME);
	}
}
