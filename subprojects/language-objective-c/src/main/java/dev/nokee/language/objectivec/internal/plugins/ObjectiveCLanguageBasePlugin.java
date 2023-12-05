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
import dev.nokee.language.nativebase.internal.ExtendsFromParentNativeSourcesRuleEx;
import dev.nokee.language.nativebase.internal.LanguageNativeBasePlugin;
import dev.nokee.language.nativebase.internal.NativeHeaderLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAware;
import dev.nokee.language.nativebase.internal.NativeSourcesMixInRule;
import dev.nokee.language.nativebase.internal.SupportLanguageSourceSet;
import dev.nokee.language.nativebase.internal.UseConventionalLayout;
import dev.nokee.language.nativebase.internal.WireParentSourceToSourceSetAction;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivec.HasObjectiveCSources;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Named;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

import static dev.nokee.language.nativebase.internal.SupportLanguageSourceSet.hasLanguageSupport;
import static dev.nokee.model.internal.ModelElementAction.withElement;
import static dev.nokee.model.internal.TypeFilteringAction.ofType;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;

public class ObjectiveCLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(NativeHeaderLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(ObjectiveCSourceSetSpec.class);

		DefaultImporter.forProject(project)
			.defaultImport(ObjectiveCSourceSet.class);

		model(project, objects()).configureEach(new NativeSourcesMixInRule<>(new NativeSourcesMixInRule.Spec("objectiveCSources", HasObjectiveCSources.class, HasObjectiveCSources::getObjectiveCSources, project.getObjects()), new NativeSourcesMixInRule.Spec("privateHeaders", HasPrivateHeaders.class, HasPrivateHeaders::getPrivateHeaders, project.getObjects())));

		model(project, objects()).configureEach(ofType(Named.class, new UseConventionalLayout<>("objectiveCSources", "src/%s/objectiveC", "src/%s/objc")));
		model(project, objects()).configureEach(ofType(Named.class, new UseConventionalLayout<>("privateHeaders", "src/%s/headers")));

		model(project, objects()).configureEach(ofType(ExtensionAware.class, withElement(new ExtendsFromParentNativeSourcesRuleEx("objectiveCSources"))));
		model(project, objects()).configureEach(ofType(ExtensionAware.class, withElement(new ExtendsFromParentNativeSourcesRuleEx("privateHeaders"))));

		// No need to register anything as ObjectiveCSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.

		model(project, objects()).configureEach(ofType(NativeLanguageSourceSetAware.class, withElement((identifier, target) -> {
			final Class<? extends SupportLanguageSourceSet> sourceSetTag = SupportObjectiveCSourceSetTag.class;
			final ElementName name = ElementName.of("objectiveC");
			final Class<? extends LanguageSourceSet> sourceSetType = ObjectiveCSourceSetSpec.class;

			if (identifier.getParents().anyMatch(hasLanguageSupport(sourceSetTag))) {
				model(project, registryOf(LanguageSourceSet.class)).register(identifier.getIdentifier().child(name), sourceSetType);
			}
		})));

		variants(project).configureEach(new WireParentSourceToSourceSetAction<>(ObjectiveCSourceSetSpec.class, "objectiveCSources"));
	}
}
