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
import dev.nokee.language.base.internal.SourcePropertyName;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.HasCSources;
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
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Named;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

import static dev.nokee.language.nativebase.internal.NativeHeaderLanguageBasePlugin.PRIVATE_HEADERS;
import static dev.nokee.language.nativebase.internal.SupportLanguageSourceSet.hasLanguageSupport;
import static dev.nokee.model.internal.ModelElementAction.withElement;
import static dev.nokee.model.internal.TypeFilteringAction.ofType;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;

public class CLanguageBasePlugin implements Plugin<Project> {
	public static final SourcePropertyName C_SOURCES = () -> "cSources";

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(NativeHeaderLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(CSourceSetSpec.class);

		DefaultImporter.forProject(project)
			.defaultImport(CSourceSet.class);

		// No need to register anything as CHeaderSet and CSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.

		model(project, objects()).configureEach(new NativeSourcesMixInRule<>(new NativeSourcesMixInRule.Spec(C_SOURCES, HasCSources.class, HasCSources::getCSources, project.getObjects()), new NativeSourcesMixInRule.Spec(PRIVATE_HEADERS, HasPrivateHeaders.class, HasPrivateHeaders::getPrivateHeaders, project.getObjects())));

		model(project, objects()).configureEach(ofType(Named.class,
			new UseConventionalLayout<>(C_SOURCES, "src/%s/c")));
		model(project, objects()).configureEach(ofType(Named.class,
			new UseConventionalLayout<>(PRIVATE_HEADERS, "src/%s/headers")));

		model(project, objects()).configureEach(ofType(ExtensionAware.class,
			withElement(new ExtendsFromParentNativeSourcesRuleEx(C_SOURCES))));
		model(project, objects()).configureEach(ofType(ExtensionAware.class,
			withElement(new ExtendsFromParentNativeSourcesRuleEx(PRIVATE_HEADERS))));

		model(project, objects()).configureEach(ofType(NativeLanguageSourceSetAware.class, withElement((identifier, target) -> {
			final Class<? extends SupportLanguageSourceSet> sourceSetTag = SupportCSourceSetTag.class;
			final ElementName name = ElementName.of("c");
			final Class<? extends LanguageSourceSet> sourceSetType = CSourceSetSpec.class;

			if (identifier.getParents().anyMatch(hasLanguageSupport(sourceSetTag))) {
				model(project, registryOf(LanguageSourceSet.class)).register(identifier.getIdentifier().child(name), sourceSetType);
			}
		})));

		variants(project).configureEach(new WireParentSourceToSourceSetAction<>(CSourceSetSpec.class, C_SOURCES));
	}
}
