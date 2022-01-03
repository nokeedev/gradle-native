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
import dev.nokee.language.base.internal.*;
import dev.nokee.model.internal.core.IsModelProperty;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.model.internal.core.ModelElements.whenElementDiscovered;

public class LanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);

		DefaultImporter.forProject(project).defaultImport(LanguageSourceSet.class);

		project.getExtensions().add("__nokee_sourceSetFactory", new SourceSetFactory(project.getObjects()));
		project.getExtensions().add("__nokee_languageSourceSetFactory", new LanguageSourceSetRegistrationFactory(project.getObjects(), project.getExtensions().getByType(SourceSetFactory.class), new SourcePropertyRegistrationActionFactory(() -> project.getExtensions().getByType(ModelRegistry.class))));
		project.getExtensions().getByType(ModelConfigurer.class).configure(whenElementDiscovered(new HasConfigurableSourceMixInRule(project.getExtensions().getByType(SourceSetFactory.class)::sourceSet, project.getExtensions().getByType(ModelRegistry.class))));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.ofProjection(LanguageSourceSet.class).asKnownObject(), (entity, knownSourceSet) -> {
			if (!entity.hasComponent(IsModelProperty.class)) {
				entity.addComponent(IsLanguageSourceSet.tag());
			}
		}));
	}
}
