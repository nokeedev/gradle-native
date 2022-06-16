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
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.javadoc.Javadoc;

public class JavadocSourcesOption implements Action<Javadoc> {
	private static final String SOURCES_EXTENSION_NAME = "sources";
	private final Project project;

	JavadocSourcesOption(Project project) {
		this.project = project;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public void execute(Javadoc task) {
		final ConfigurableFileCollection sources = project.getObjects().fileCollection();
		task.getExtensions().add(ConfigurableFileCollection.class, SOURCES_EXTENSION_NAME, sources);
		task.getInputs().files(sources).ignoreEmptyDirectories().withPathSensitivity(PathSensitivity.RELATIVE);
	}

	public static ConfigurableFileCollection sources(Javadoc self) {
		return (ConfigurableFileCollection) self.getExtensions().getByName(SOURCES_EXTENSION_NAME);
	}
}
