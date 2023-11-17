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

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import dev.nokee.model.internal.DefaultModelObjects;
import dev.nokee.model.internal.ModelExtension;
import dev.nokee.model.internal.ModelMap;
import dev.nokee.model.internal.ModelMapAdapters;
import dev.nokee.model.internal.ModelObjectFactoryRegistry;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.ModelObjects;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.names.NamesCapabilityPlugin;
import dev.nokee.model.internal.properties.ModelPropertiesCapabilityPlugin;
import dev.nokee.model.internal.registry.DefaultModelRegistry;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.model.internal.tasks.ModelReportTask;
import dev.nokee.utils.ActionUtils;
import dev.nokee.utils.TaskUtils;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Rule;
import org.gradle.api.Task;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.MutationGuards;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;
import java.util.List;

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
		modelRegistry.configure(new GenerateModelPathFromParents());
		modelRegistry.configure(new GenerateModelPathFromIdentifier());

		target.getPluginManager().apply(NamesCapabilityPlugin.class);

		modelRegistry.get(ModelPath.root()).addComponent(new DisplayNameComponent(target.toString()));

		target.getExtensions().create("model", ModelExtension.class);
	}

	private void applyToSettings(Settings settings) {
		applyToAllTarget(settings);
	}

	@SuppressWarnings("unchecked")
	private void applyToProject(Project project) {
		project.getConfigurations().all(ActionUtils.doNothing()); // Because... don't get me started with this... :'(

		applyToAllTarget(project);

		project.getTasks().register("nokeeModel", ModelReportTask.class, TaskUtils.configureDescription("Displays the configuration model of %s.", project));

		project.getExtensions().getByType(ModelLookup.class).get(ModelPath.root()).addComponent(new IdentifierComponent(ProjectIdentifier.of(project)));

		final ModelObjects objects = model(project).getExtensions().create("$objects", DefaultModelObjects.class);
		objects.register(model(project).getExtensions().create("$configuration", ModelMapAdapters.ForConfigurationContainer.class, project.getConfigurations()));
		objects.register(model(project).getExtensions().create("$tasks", ModelMapAdapters.ForPolymorphicDomainObjectContainer.class, Task.class, new Task.Namer(), project.getTasks()));

		project.getTasks().addRule(new Rule() {
			@Override
			public String getDescription() {
				return "discover tasks";
			}

			@Override
			public void apply(String domainObjectName) {
				List<ModelNode> candidateEntities = null;
				while(!(candidateEntities = project.getExtensions().getByType(ModelLookup.class).query(it -> it.hasComponent(ModelTags.typeOf(ConfigurableTag.class)) && !it.has(ModelState.IsAtLeastRealized.class)).get()).isEmpty()) {
					for (ModelNode entity : candidateEntities) {
						MutationGuards.of(project.getTasks()).withMutationEnabled(__ -> {
							ModelStates.realize(entity);
						}).execute(null);
						if (project.getTasks().getNames().contains(domainObjectName)) {
							return; // found
						}
					}
				}
			}
		});
	}

	public static ModelExtension model(ExtensionAware target) {
		return target.getExtensions().getByType(ModelExtension.class);
	}

	public static <S> S model(ExtensionAware target, TypeOf<S> type) {
		return model(target).getExtensions().getByType(type);
	}

	public static TypeOf<ModelObjects> objects() {
		return TypeOf.typeOf(ModelObjects.class);
	}

	public static <S> TypeOf<ModelObjectRegistry<S>> registryOf(Class<S> type) {
		return TypeOf.typeOf(new TypeToken<ModelObjectRegistry<S>>() {}.where(new TypeParameter<S>() {}, type).getType());
	}

	public static <S> TypeOf<ModelObjectFactoryRegistry<S>> factoryRegistryOf(Class<S> type) {
		return TypeOf.typeOf(new TypeToken<ModelObjectFactoryRegistry<S>>() {}.where(new TypeParameter<S>() {}, type).getType());
	}

	public static <S> TypeOf<ModelMap<S>> mapOf(Class<S> type) {
		return TypeOf.typeOf(new TypeToken<ModelMap<S>>() {}.where(new TypeParameter<S>() {}, type).getType());
	}
}
