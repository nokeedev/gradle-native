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
package dev.nokee.language.cpp.internal.plugins;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.cpp.HasCppSources;
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
import dev.nokee.scripts.DefaultImporter;
import dev.nokee.utils.Optionals;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.instantiator;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;
import static dev.nokee.utils.Optionals.safeAs;

public class CppLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(NativeHeaderLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(CppSourceSetSpec.class, name -> {
			return instantiator(project).newInstance(CppSourceSetSpec.class);
		});

		DefaultImporter.forProject(project)
			.defaultImport(CppSourceSet.class);

		// No need to register anything as CppHeaderSet and CppSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.

		components(project).configureEach(new NativeSourcesMixInRule<>(new NativeSourcesMixInRule.Spec("cppSources", HasCppSources.class, HasCppSources::getCppSources, project.getObjects()), new NativeSourcesMixInRule.Spec("privateHeaders", HasPrivateHeaders.class, HasPrivateHeaders::getPrivateHeaders, project.getObjects())));
		variants(project).configureEach(new NativeSourcesMixInRule<>(new NativeSourcesMixInRule.Spec("cppSources", HasCppSources.class, HasCppSources::getCppSources, project.getObjects()), new NativeSourcesMixInRule.Spec("privateHeaders", HasPrivateHeaders.class, HasPrivateHeaders::getPrivateHeaders, project.getObjects())));

		components(project).configureEach(new UseConventionalLayout<>("cppSources", "src/%s/cpp"));
		variants(project).configureEach(new UseConventionalLayout<>("cppSources", "src/%s/cpp"));
		components(project).configureEach(new UseConventionalLayout<>("privateHeaders", "src/%s/headers"));
		variants(project).configureEach(new UseConventionalLayout<>("privateHeaders", "src/%s/headers"));

		components(project).configureEach(new ExtendsFromParentNativeSourcesRule<>("cppSources"));
		components(project).configureEach(new ExtendsFromParentNativeSourcesRule<>("privateHeaders"));

		model(project, objects()).configureEach((identifier, target) -> {
			if (target instanceof NativeLanguageSourceSetAware) {
				final Class<?> sourceSetTag = SupportCppSourceSetTag.class;
				final ElementName name = ElementName.of("cpp");
				final Class<? extends LanguageSourceSet> sourceSetType = CppSourceSetSpec.class;

				if (identifier.getParents().anyMatch(t -> t.instanceOf(sourceSetTag) || Optionals.stream(t.getAsOptional().map(safeAs(ExtensionAware.class))).anyMatch(it -> it.getExtensions().findByType(sourceSetTag) != null)) || project.getExtensions().findByType(sourceSetTag) != null) {
					model(project, registryOf(LanguageSourceSet.class)).register(identifier.getIdentifier().child(name), sourceSetType);
				}
			}
		});

		variants(project).configureEach(new WireParentSourceToSourceSetAction<>(CppSourceSetSpec.class, "cppSources"));
	}
}
