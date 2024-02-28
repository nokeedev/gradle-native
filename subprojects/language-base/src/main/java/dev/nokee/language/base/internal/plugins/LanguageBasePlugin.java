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
import dev.nokee.language.base.internal.LanguagePropertiesAware;
import dev.nokee.language.base.internal.LanguageSourcePropertySpec;
import dev.nokee.language.base.internal.LanguageSupportSpec;
import dev.nokee.language.base.internal.PropertySpec;
import dev.nokee.language.base.internal.SourceProperty;
import dev.nokee.language.base.internal.SourcePropertyAware;
import dev.nokee.language.base.internal.rules.RegisterLanguageImplementationRule;
import dev.nokee.language.base.internal.rules.RegisterSourcePropertyAsGradleExtensionRule;
import dev.nokee.language.base.internal.rules.RegisterSourcePropertyBasedOnLanguageImplementationRule;
import dev.nokee.language.base.internal.rules.SourcePropertiesExtendsFromParentRule;
import dev.nokee.language.base.internal.rules.UseConventionalLayoutRule;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelMap;
import dev.nokee.model.internal.ModelMapFactory;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.platform.base.View;
import dev.nokee.platform.base.internal.ModelNodeBackedViewStrategy;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.reflect.TypeOf;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static dev.nokee.model.internal.ModelElementAction.withElement;
import static dev.nokee.model.internal.TypeFilteringAction.ofType;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.instantiator;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.mapOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;

public class LanguageBasePlugin implements Plugin<Project> {
	private static final TypeOf<ExtensiblePolymorphicDomainObjectContainer<LanguageSourceSet>> LANGUAGE_SOURCE_SET_CONTAINER_TYPE = new TypeOf<ExtensiblePolymorphicDomainObjectContainer<LanguageSourceSet>>() {};

	public static ModelMap<LanguageSourceSet> sources(Project project) {
		return model(project, mapOf(LanguageSourceSet.class));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);

		{
			final ExtensiblePolymorphicDomainObjectContainer<LanguageSourceSet> container = project.getObjects().polymorphicDomainObjectContainer(LanguageSourceSet.class);
			project.getExtensions().add(LANGUAGE_SOURCE_SET_CONTAINER_TYPE, "sources", container);
			model(project).getExtensions().add("sources", model(project).getExtensions().getByType(ModelMapFactory.class).create(LanguageSourceSet.class, container));
		}

		DefaultImporter.forProject(project).defaultImport(LanguageSourceSet.class);

		final Factory<View<LanguageSourceSet>> sourcesFactory = () -> {
			final ModelObjectIdentifier identifier = ModelElementSupport.nextIdentifier();
			return instantiator(project).newInstance(ViewAdapter.class, LanguageSourceSet.class, new ModelNodeBackedViewStrategy(model(project, mapOf(LanguageSourceSet.class)), identifier));
		};
		model(project).getExtensions().add(new TypeOf<Factory<View<LanguageSourceSet>>>() {}, "__nokee_sourcesFactory", sourcesFactory);

		model(project, objects()).configureEach(ofType(LanguagePropertiesAware.class, new RegisterSourcePropertyAsGradleExtensionRule()));
		model(project, objects()).configureEach(ofType(LanguageSupportSpec.class, new RegisterSourcePropertyBasedOnLanguageImplementationRule()));
		model(project, objects()).configureEach(ofType(LanguagePropertiesAware.class, new SourcePropertiesExtendsFromParentRule()));
		model(project, objects()).configureEach(ofType(LanguageSupportSpec.class, new RegisterLanguageImplementationRule(instantiator(project))));

		model(project).getExtensions().add("$properties", model(project).getExtensions().getByType(ModelMapFactory.class).create(PropertySpec.class, project.getObjects().polymorphicDomainObjectContainer(PropertySpec.class)));
		model(project, factoryRegistryOf(PropertySpec.class)).registerFactory(SourceProperty.class);
		model(project, mapOf(PropertySpec.class)).configureEach(LanguageSourcePropertySpec.class, new UseConventionalLayoutRule());
		model(project, mapOf(LanguageSourceSet.class)).configureEach(ofType(SourcePropertyAware.class, withElement((element, sourceSet, source) -> {
			source.getSource().from((Callable<?>) () -> {
				return element.getParents().flatMap(it -> it.safeAs(ExtensionAware.class).map(t -> t.getExtensions().findByName(element.getIdentifier().getName().toString() + "Sources")).map(Stream::of).getOrElse(Stream.empty())).findFirst().map(it -> (Iterable<?>) it).orElse(Collections.emptyList());
			});
		})));
	}
}
