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
import dev.nokee.language.nativebase.internal.ExtendsFromParentNativeSourcesRuleEx;
import dev.nokee.language.nativebase.internal.LanguageNativeBasePlugin;
import dev.nokee.language.nativebase.internal.NativeHeaderLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAware;
import dev.nokee.language.nativebase.internal.NativeSourcesMixInRule;
import dev.nokee.language.nativebase.internal.UseConventionalLayout;
import dev.nokee.language.nativebase.internal.WireParentSourceToSourceSetAction;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Named;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

import static dev.nokee.model.internal.ModelElementAction.withElement;
import static dev.nokee.model.internal.TypeFilteringAction.ofType;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;

public class CppLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(NativeHeaderLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(CppSourceSetSpec.class);

		DefaultImporter.forProject(project)
			.defaultImport(CppSourceSet.class);

		// No need to register anything as CppHeaderSet and CppSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.

		model(project, objects()).configureEach(new NativeSourcesMixInRule<>(new NativeSourcesMixInRule.Spec("cppSources", HasCppSources.class, HasCppSources::getCppSources, project.getObjects()), new NativeSourcesMixInRule.Spec("privateHeaders", HasPrivateHeaders.class, HasPrivateHeaders::getPrivateHeaders, project.getObjects())));

		model(project, objects()).configureEach(ofType(Named.class, new UseConventionalLayout<>("cppSources", "src/%s/cpp")));
		model(project, objects()).configureEach(ofType(Named.class, new UseConventionalLayout<>("privateHeaders", "src/%s/headers")));

		model(project, objects()).configureEach(ofType(ExtensionAware.class, withElement(new ExtendsFromParentNativeSourcesRuleEx("cppSources"))));
		model(project, objects()).configureEach(ofType(ExtensionAware.class, withElement(new ExtendsFromParentNativeSourcesRuleEx("privateHeaders"))));

		model(project, objects()).configureEach(ofType(NativeLanguageSourceSetAware.class, withElement((identifier, target) -> {
			final Class<?> sourceSetTag = SupportCppSourceSetTag.class;
			final ElementName name = ElementName.of("cpp");
			final Class<? extends LanguageSourceSet> sourceSetType = CppSourceSetSpec.class;

			if (identifier.getParents().anyMatch(t -> t.instanceOf(sourceSetTag) || t.safeAs(ExtensionAware.class).map(it -> it.getExtensions().findByType(sourceSetTag) != null).getOrElse(false))) {
				model(project, registryOf(LanguageSourceSet.class)).register(identifier.getIdentifier().child(name), sourceSetType);
			}
		})));

		variants(project).configureEach(new WireParentSourceToSourceSetAction<>(CppSourceSetSpec.class, "cppSources"));
	}
}
