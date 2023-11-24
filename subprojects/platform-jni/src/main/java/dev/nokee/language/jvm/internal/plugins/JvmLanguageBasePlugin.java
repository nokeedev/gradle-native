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
package dev.nokee.language.jvm.internal.plugins;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.jvm.internal.GroovySourceSetSpec;
import dev.nokee.language.jvm.internal.JavaSourceSetSpec;
import dev.nokee.language.jvm.internal.KotlinSourceSetSpec;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.SourceSetContainer;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.instantiator;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;

public class JvmLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageBasePlugin.class);

		project.getPlugins().withType(JavaBasePlugin.class, ignored -> {
			model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(GroovySourceSetSpec.class, name -> {
				return instantiator(project).newInstance(GroovySourceSetSpec.class, project.getExtensions().getByType(SourceSetContainer.class), model(project, registryOf(Task.class)));
			});
			model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(JavaSourceSetSpec.class, name -> {
				return instantiator(project).newInstance(JavaSourceSetSpec.class, project.getExtensions().getByType(SourceSetContainer.class), model(project, registryOf(Task.class)));
			});
			model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(KotlinSourceSetSpec.class, name -> {
				return instantiator(project).newInstance(KotlinSourceSetSpec.class, project.getExtensions().getByType(SourceSetContainer.class), model(project, registryOf(Task.class)));
			});
		});
	}
}
