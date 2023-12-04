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
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.scripts.DefaultImporter;
import dev.nokee.utils.Optionals;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

import static dev.nokee.model.internal.ModelElementAction.withElement;
import static dev.nokee.model.internal.TypeFilteringAction.ofType;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;
import static dev.nokee.utils.Optionals.safeAs;

public class ObjectiveCLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(NativeHeaderLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(ObjectiveCSourceSetSpec.class);

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

		model(project, objects()).configureEach(ofType(NativeLanguageSourceSetAware.class, withElement((identifier, target) -> {
			final Class<?> sourceSetTag = SupportObjectiveCSourceSetTag.class;
			final ElementName name = ElementName.of("objectiveC");
			final Class<? extends LanguageSourceSet> sourceSetType = ObjectiveCSourceSetSpec.class;

			if (identifier.getParents().anyMatch(t -> t.instanceOf(sourceSetTag) || t.safeAs(ExtensionAware.class).map(it -> it.getExtensions().findByType(sourceSetTag) != null).getOrElse(false)) || project.getExtensions().findByType(sourceSetTag) != null) {
				model(project, registryOf(LanguageSourceSet.class)).register(identifier.getIdentifier().child(name), sourceSetType);
			}
		})));

		variants(project).configureEach(new WireParentSourceToSourceSetAction<>(ObjectiveCSourceSetSpec.class, "objectiveCSources"));
	}
}
