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

import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.base.internal.SourceViewAdapter;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelMapAdapters;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.decorators.ModelDecorator;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.platform.base.internal.ModelNodeBackedViewStrategy;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Named;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.instantiator;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;

public class LanguageBasePlugin implements Plugin<Project> {
	private static final org.gradle.api.reflect.TypeOf<ExtensiblePolymorphicDomainObjectContainer<LanguageSourceSet>> LANGUAGE_SOURCE_SET_CONTAINER_TYPE = new org.gradle.api.reflect.TypeOf<ExtensiblePolymorphicDomainObjectContainer<LanguageSourceSet>>() {};

	public static ExtensiblePolymorphicDomainObjectContainer<LanguageSourceSet> sources(Project project) {
		return project.getExtensions().getByType(LANGUAGE_SOURCE_SET_CONTAINER_TYPE);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);

		project.getExtensions().add(LANGUAGE_SOURCE_SET_CONTAINER_TYPE, "$sources", project.getObjects().polymorphicDomainObjectContainer(LanguageSourceSet.class));
		model(project, objects()).register(model(project).getExtensions().create("sources", ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, LanguageSourceSet.class, sources(project), project.getExtensions().getByType(ModelDecorator.class), ProjectIdentifier.of(project), instantiator(project)));

		DefaultImporter.forProject(project).defaultImport(LanguageSourceSet.class);

		final Factory<SourceView<LanguageSourceSet>> sourcesFactory = () -> {
			Named.Namer namer = new Named.Namer();
			ModelObjectIdentifier identifier = ModelElementSupport.nextIdentifier();
			Runnable realizeNow = () -> {};
			return new SourceViewAdapter<>(new ViewAdapter<>(LanguageSourceSet.class, new ModelNodeBackedViewStrategy(it -> namer.determineName((LanguageSourceSet) it), sources(project), project.getProviders(), project.getObjects(), realizeNow, identifier)));
		};
		project.getExtensions().add(new org.gradle.api.reflect.TypeOf<Factory<SourceView<LanguageSourceSet>>>() {}, "__nokee_sourcesFactory", sourcesFactory);
	}
}
