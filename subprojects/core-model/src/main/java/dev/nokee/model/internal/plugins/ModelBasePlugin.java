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
import dev.nokee.utils.ActionUtils;
import dev.nokee.utils.TaskUtils;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;

import javax.inject.Inject;

public class ModelBasePlugin<T extends PluginAware & ExtensionAware> implements Plugin<T> {
	private final PluginTargetSupport pluginScopes = PluginTargetSupport.builder()
		.withPluginId("dev.nokee.model-base")
		.forTarget(Settings.class, this::applyToSettings)
		.forTarget(Project.class, this::applyToProject)
		.build();
	private final ObjectFactory objects;

	@Inject
	ModelBasePlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(T target) {
		pluginScopes.apply(target);
	}

	private <S extends PluginAware & ExtensionAware> void applyToAllTarget(S target) {
		val modelRegistry = new DefaultModelRegistry(objects::newInstance);
		target.getExtensions().add(ModelRegistry.class, "__NOKEE_modelRegistry", modelRegistry);
		target.getExtensions().add(ModelLookup.class, "__NOKEE_modelLookup", modelRegistry);
		target.getExtensions().add(ModelConfigurer.class, "__NOKEE_modelConfigurer", modelRegistry);
		target.getExtensions().add(ModelPropertyRegistrationFactory.class, "__NOKEE_modelPropertyRegistrationFactory", new ModelPropertyRegistrationFactory());

		modelRegistry.configure(new AttachDisplayNameToGradleProperty());
		target.getPluginManager().apply(ModelPropertiesCapabilityPlugin.class);
		target.getPluginManager().apply(ModelActionSystem.class);
		modelRegistry.configure(new GenerateModelPathFromParents());
		modelRegistry.configure(new GenerateModelPathFromIdentifier());

		target.getPluginManager().apply(AncestryCapabilityPlugin.class);
		target.getPluginManager().apply(NamesCapabilityPlugin.class);
	}

	private void applyToSettings(Settings settings) {
		applyToAllTarget(settings);
	}

	private void applyToProject(Project project) {
		project.getConfigurations().all(ActionUtils.doNothing()); // Because... don't get me started with this... :'(

		applyToAllTarget(project);

		project.getTasks().register("nokeeModel", ModelReportTask.class, TaskUtils.configureDescription("Displays the configuration model of %s.", project));

		project.getExtensions().getByType(ModelLookup.class).get(ModelPath.root()).addComponent(new IdentifierComponent(ProjectIdentifier.of(project)));
	}
}
