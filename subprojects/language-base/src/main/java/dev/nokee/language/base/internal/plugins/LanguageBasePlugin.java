/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.language.base.internal.plugins;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.BaseLanguageSourceSetProjection;
import dev.nokee.language.base.internal.BridgedLanguageSourceSetProjection;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;

import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.type.ModelType.of;

public class LanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);

		DefaultImporter.forProject(project).defaultImport(LanguageSourceSet.class);
	}

	public static <T extends LanguageSourceSet> NodeRegistration sourceSet(String name, Class<T> publicType) {
		return NodeRegistration.of(name, of(publicType))
			.withComponent(managed(of(BaseLanguageSourceSetProjection.class)));
	}

	public static <T extends LanguageSourceSet> NodeRegistration bridgeSourceSet(SourceDirectorySet from, Class<T> to) {
		return NodeRegistration.of(from.getName(), of(to))
			.withComponent(managed(of(BridgedLanguageSourceSetProjection.class), from));
	}
}
