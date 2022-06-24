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
package dev.nokee.model.internal.plugins;

import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.actions.ModelActionSystem;
import dev.nokee.model.internal.ancestors.AncestryCapabilityPlugin;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.names.NamesCapabilityPlugin;
import dev.nokee.model.internal.properties.ModelPropertiesCapabilityPlugin;
import dev.nokee.model.internal.registry.DefaultModelRegistry;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.tasks.ModelReportTask;
import dev.nokee.utils.TaskUtils;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ModelBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		val modelRegistry = new DefaultModelRegistry(project.getObjects()::newInstance);
		project.getExtensions().add(ModelRegistry.class, "__NOKEE_modelRegistry", modelRegistry);
		project.getExtensions().add(ModelLookup.class, "__NOKEE_modelLookup", modelRegistry);
		project.getExtensions().add(ModelConfigurer.class, "__NOKEE_modelConfigurer", modelRegistry);
		project.getExtensions().add(ModelPropertyRegistrationFactory.class, "__NOKEE_modelPropertyRegistrationFactory", new ModelPropertyRegistrationFactory());

		project.getTasks().register("nokeeModel", ModelReportTask.class, TaskUtils.configureDescription("Displays the configuration model of %s.", project));

		modelRegistry.configure(new AttachDisplayNameToGradleProperty());
		project.getPluginManager().apply(ModelPropertiesCapabilityPlugin.class);
		new ModelActionSystem().execute(project);
		modelRegistry.configure(new GenerateModelPathFromParents());
		modelRegistry.configure(new GenerateModelPathFromIdentifier());

		project.getPluginManager().apply(AncestryCapabilityPlugin.class);
		project.getPluginManager().apply(NamesCapabilityPlugin.class);

		modelRegistry.get(ModelPath.root()).addComponent(new IdentifierComponent(ProjectIdentifier.of(project)));
	}
}
