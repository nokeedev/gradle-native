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
import dev.nokee.model.internal.decorators.MutableModelDecorator;
import dev.nokee.utils.ActionUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.reflect.TypeOf;

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
		target.getExtensions().create("model", ModelExtension.class);
	}

	private void applyToSettings(Settings settings) {
		applyToAllTarget(settings);
	}

	@SuppressWarnings("unchecked")
	private void applyToProject(Project project) {
		project.getConfigurations().all(ActionUtils.doNothing()); // Because... don't get me started with this... :'(

		applyToAllTarget(project);

		final ModelObjects objects = model(project).getExtensions().create("$objects", DefaultModelObjects.class);
		objects.register(model(project).getExtensions().create("$configuration", ModelMapAdapters.ForConfigurationContainer.class, project.getConfigurations()));
		objects.register(model(project).getExtensions().create("$tasks", ModelMapAdapters.ForTaskContainer.class, project.getTasks()));

		project.getExtensions().create("__nokee_modelDecorator", MutableModelDecorator.class);
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
