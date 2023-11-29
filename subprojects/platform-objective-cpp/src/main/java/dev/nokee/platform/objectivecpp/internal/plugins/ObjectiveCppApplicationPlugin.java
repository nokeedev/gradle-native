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
package dev.nokee.platform.objectivecpp.internal.plugins;

import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppLanguageBasePlugin;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.objectivecpp.internal.DefaultObjectiveCppApplication;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static dev.nokee.model.internal.names.ElementName.ofMain;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.instantiator;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.BaseNameActions.baseName;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;

public class ObjectiveCppApplicationPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "application";
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public ObjectiveCppApplicationPlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		// Create the component
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(ObjectiveCppLanguageBasePlugin.class);

		model(project, factoryRegistryOf(Component.class)).registerFactory(DefaultObjectiveCppApplication.class, name -> {
			return instantiator(project).newInstance(DefaultObjectiveCppApplication.class);
		});
		model(project, factoryRegistryOf(Variant.class)).registerFactory(DefaultObjectiveCppApplication.Variant.class, name -> {
			return instantiator(project).newInstance(DefaultObjectiveCppApplication.Variant.class);
		});

		final NamedDomainObjectProvider<DefaultObjectiveCppApplication> componentProvider = model(project, registryOf(Component.class)).register(ProjectIdentifier.of(project).child(ofMain()), DefaultObjectiveCppApplication.class).asProvider();
		componentProvider.configure(baseName(convention(project.getName())));
		val extension = componentProvider.get();

		project.getExtensions().add(EXTENSION_NAME, extension);
	}
}
