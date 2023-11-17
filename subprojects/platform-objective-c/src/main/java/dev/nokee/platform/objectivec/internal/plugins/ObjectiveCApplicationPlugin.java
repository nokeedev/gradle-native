/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.objectivec.internal.plugins;

import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguageBasePlugin;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.DefaultVariantDimensions;
import dev.nokee.platform.base.internal.VariantViewFactory;
import dev.nokee.platform.nativebase.internal.NativeApplicationComponentModelRegistrationFactory;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.objectivec.ObjectiveCApplication;
import dev.nokee.platform.objectivec.internal.ObjectiveCApplicationSpec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;

import static dev.nokee.model.internal.names.ElementName.ofMain;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.BaseNameActions.baseName;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;

public class ObjectiveCApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public ObjectiveCApplicationPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(ObjectiveCLanguageBasePlugin.class);

		model(project, factoryRegistryOf(Component.class)).registerFactory(ObjectiveCApplicationSpec.class, name -> {
			return project.getObjects().newInstance(ObjectiveCApplicationSpec.class, model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<BinaryView<Binary>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<SourceView<LanguageSourceSet>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<TaskView<Task>>>() {}), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(new TypeOf<Factory<DefaultVariantDimensions>>() {}));
		});

		final NamedDomainObjectProvider<ObjectiveCApplicationSpec> componentProvider = model(project, registryOf(Component.class)).register(ProjectIdentifier.of(project).child(ofMain()), ObjectiveCApplicationSpec.class).asProvider();
		componentProvider.configure(baseName(convention(project.getName())));
		val extension = componentProvider.get();

		project.getExtensions().add(ObjectiveCApplication.class, EXTENSION_NAME, extension);
	}

	public static ModelRegistration objectiveCApplication(String name, Project project) {
		val identifier = ModelObjectIdentifier.builder().name(name.equals("main") ? ofMain() : ElementName.of(name)).withParent(ProjectIdentifier.of(project)).build();
		return new NativeApplicationComponentModelRegistrationFactory(ObjectiveCApplicationSpec.class, project).create(identifier).build();
	}
}
