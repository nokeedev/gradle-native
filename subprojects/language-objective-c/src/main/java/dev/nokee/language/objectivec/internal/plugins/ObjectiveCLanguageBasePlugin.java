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
package dev.nokee.language.objectivec.internal.plugins;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.nativebase.HasPrivateHeaders;
import dev.nokee.language.nativebase.internal.ExtendsFromParentNativeSourcesRule;
import dev.nokee.language.nativebase.internal.LanguageNativeBasePlugin;
import dev.nokee.language.nativebase.internal.NativeHeaderLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAware;
import dev.nokee.language.nativebase.internal.NativeSourcesMixInRule;
import dev.nokee.language.nativebase.internal.UseConventionalLayout;
import dev.nokee.language.nativebase.internal.WireParentSourceToSourceSetAction;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivec.HasObjectiveCSources;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.ModelObjectFactory;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionAware;

import java.util.Optional;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;

public class ObjectiveCLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(NativeHeaderLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(ObjectiveCSourceSetSpec.class, new ModelObjectFactory<ObjectiveCSourceSetSpec>(project, IsLanguageSourceSet.class) {
			@Override
			protected ObjectiveCSourceSetSpec doCreate(String name) {
				return project.getObjects().newInstance(ObjectiveCSourceSetSpec.class, model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)));
			}
		});

		DefaultImporter.forProject(project)
			.defaultImport(ObjectiveCSourceSet.class);

		components(project).configureEach(new NativeSourcesMixInRule<>(new NativeSourcesMixInRule.Spec("objectiveCSources", HasObjectiveCSources.class, HasObjectiveCSources::getObjectiveCSources, project.getObjects()), new NativeSourcesMixInRule.Spec("privateHeaders", HasPrivateHeaders.class, HasPrivateHeaders::getPrivateHeaders, project.getObjects())));
		variants(project).configureEach(new NativeSourcesMixInRule<>(new NativeSourcesMixInRule.Spec("objectiveCSources", HasObjectiveCSources.class, HasObjectiveCSources::getObjectiveCSources, project.getObjects()), new NativeSourcesMixInRule.Spec("privateHeaders", HasPrivateHeaders.class, HasPrivateHeaders::getPrivateHeaders, project.getObjects())));

		components(project).configureEach(new UseConventionalLayout<>("objectiveCSources", "src/%s/objectiveC", "src/%s/objc"));
		variants(project).configureEach(new UseConventionalLayout<>("objectiveCSources", "src/%s/objectiveC", "src/%s/objc"));
		components(project).configureEach(new UseConventionalLayout<>("privateHeaders", "src/%s/headers"));
		variants(project).configureEach(new UseConventionalLayout<>("privateHeaders", "src/%s/headers"));

		components(project).configureEach(new ExtendsFromParentNativeSourcesRule<>("objectiveCSources"));
		components(project).configureEach(new ExtendsFromParentNativeSourcesRule<>("privateHeaders"));

		// No need to register anything as ObjectiveCSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.

		model(project, objects()).configureEach((identifier, target) -> {
			if (target instanceof NativeLanguageSourceSetAware) {
				final Class<? extends ModelTag> sourceSetTag = SupportObjectiveCSourceSetTag.class;
				final ElementName name = ElementName.of("objectiveC");
				final Class<? extends LanguageSourceSet> sourceSetType = ObjectiveCSourceSetSpec.class;

				if (model(project, objects()).parentsOf(identifier).anyMatch(it -> Optional.ofNullable(((ExtensionAware) it.get()).getExtensions().findByType(ModelNode.class)).map(t -> t.hasComponent(ModelTags.typeOf(sourceSetTag))).orElseGet(() -> project.getExtensions().getByType(ModelLookup.class).get(ModelPath.root()).hasComponent(ModelTags.typeOf(sourceSetTag))))) {
					model(project, registryOf(LanguageSourceSet.class)).register(identifier.child(name), sourceSetType);
				}
			}
		});

		variants(project).configureEach(new WireParentSourceToSourceSetAction<>(ObjectiveCSourceSetSpec.class, "objectiveCSources"));
	}
}
