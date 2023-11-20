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
package dev.nokee.language.c.internal.plugins;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.HasCSources;
import dev.nokee.language.nativebase.HasPrivateHeaders;
import dev.nokee.language.nativebase.internal.ExtendsFromParentNativeSourcesRule;
import dev.nokee.language.nativebase.internal.LanguageNativeBasePlugin;
import dev.nokee.language.nativebase.internal.NativeHeaderLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAware;
import dev.nokee.language.nativebase.internal.NativeSourcesMixInRule;
import dev.nokee.language.nativebase.internal.UseConventionalLayout;
import dev.nokee.language.nativebase.internal.WireParentSourceToSourceSetAction;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionAware;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;

public class CLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(NativeHeaderLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(CSourceSetSpec.class, name -> {
			return project.getObjects().newInstance(CSourceSetSpec.class, model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)));
		});

		DefaultImporter.forProject(project)
			.defaultImport(CSourceSet.class);

		// No need to register anything as CHeaderSet and CSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.

		components(project).configureEach(new NativeSourcesMixInRule<>(new NativeSourcesMixInRule.Spec("cSources", HasCSources.class, HasCSources::getCSources, project.getObjects()), new NativeSourcesMixInRule.Spec("privateHeaders", HasPrivateHeaders.class, HasPrivateHeaders::getPrivateHeaders, project.getObjects())));
		variants(project).configureEach(new NativeSourcesMixInRule<>(new NativeSourcesMixInRule.Spec("cSources", HasCSources.class, HasCSources::getCSources, project.getObjects()), new NativeSourcesMixInRule.Spec("privateHeaders", HasPrivateHeaders.class, HasPrivateHeaders::getPrivateHeaders, project.getObjects())));

		components(project).configureEach(new UseConventionalLayout<>("cSources", "src/%s/c"));
		variants(project).configureEach(new UseConventionalLayout<>("cSources", "src/%s/c"));
		components(project).configureEach(new UseConventionalLayout<>("privateHeaders", "src/%s/headers"));
		variants(project).configureEach(new UseConventionalLayout<>("privateHeaders", "src/%s/headers"));

		components(project).configureEach(new ExtendsFromParentNativeSourcesRule<>("cSources"));
		components(project).configureEach(new ExtendsFromParentNativeSourcesRule<>("privateHeaders"));

		model(project, objects()).configureEach((identifier, target) -> {
			if (target instanceof NativeLanguageSourceSetAware) {
				final Class<?> sourceSetTag = SupportCSourceSetTag.class;
				final ElementName name = ElementName.of("c");
				final Class<? extends LanguageSourceSet> sourceSetType = CSourceSetSpec.class;

				if (model(project, objects()).parentsOf(identifier).anyMatch(it -> ((ExtensionAware) it.get()).getExtensions().findByType(sourceSetTag) != null) || project.getExtensions().findByType(sourceSetTag) != null) {
					model(project, registryOf(LanguageSourceSet.class)).register(identifier.child(name), sourceSetType);
				}
			}
		});

		variants(project).configureEach(new WireParentSourceToSourceSetAction<>(CSourceSetSpec.class, "cSources"));
	}
}
