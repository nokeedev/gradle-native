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
package dev.nokee.language.swift.internal.plugins;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.nativebase.internal.ExtendsFromParentNativeSourcesRule;
import dev.nokee.language.nativebase.internal.LanguageNativeBasePlugin;
import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAware;
import dev.nokee.language.nativebase.internal.NativeSourcesMixInRule;
import dev.nokee.language.nativebase.internal.UseConventionalLayout;
import dev.nokee.language.nativebase.internal.WireParentSourceToSourceSetAction;
import dev.nokee.language.swift.HasSwiftSources;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.ModelObjectFactory;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sources;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;

public class SwiftLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(SwiftCompilerPlugin.class);

		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(SwiftSourceSetSpec.class, new ModelObjectFactory<SwiftSourceSetSpec>(project, IsLanguageSourceSet.class) {
			@Override
			protected SwiftSourceSetSpec doCreate(String name) {
				return project.getObjects().newInstance(SwiftSourceSetSpec.class, model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)));
			}
		});

		DefaultImporter.forProject(project).defaultImport(SwiftSourceSet.class);

		// No need to register anything as ObjectiveCSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.

		sources(project).withType(SwiftSourceSetSpec.class).configureEach(sourceSet -> {
			sourceSet.getImportModules().extendsFrom(sourceSet.getDependencies().getCompileOnly());
		});

		components(project).configureEach(new NativeSourcesMixInRule<>(new NativeSourcesMixInRule.Spec("swiftSources", HasSwiftSources.class, HasSwiftSources::getSwiftSources, project.getObjects())));
		variants(project).configureEach(new NativeSourcesMixInRule<>(new NativeSourcesMixInRule.Spec("swiftSources", HasSwiftSources.class, HasSwiftSources::getSwiftSources, project.getObjects())));

		components(project).configureEach(new UseConventionalLayout<>("swiftSources", "src/%s/swift"));
		variants(project).configureEach(new UseConventionalLayout<>("swiftSources", "src/%s/swift"));

		components(project).configureEach(new ExtendsFromParentNativeSourcesRule<>("swiftSources"));

		sources(project).withType(SwiftSourceSetSpec.class).configureEach(new ImportModulesConfigurationRegistrationAction(project.getObjects()));
		sources(project).withType(SwiftSourceSetSpec.class).configureEach(new AttachImportModulesToCompileTaskRule());
		sources(project).withType(SwiftSourceSetSpec.class).configureEach(new SwiftCompileTaskDefaultConfigurationRule());

		model(project, objects()).configureEach((identifier, target) -> {
			if (target instanceof NativeLanguageSourceSetAware) {
				final Class<? extends ModelTag> sourceSetTag = SupportSwiftSourceSetTag.class;
				final ElementName name = ElementName.of("swift");
				final Class<? extends LanguageSourceSet> sourceSetType = SwiftSourceSetSpec.class;

				if (model(project, objects()).parentsOf(identifier).anyMatch(it -> ((ExtensionAware) it.get()).getExtensions().findByType(sourceSetTag) != null) || project.getExtensions().findByType(sourceSetTag) != null) {
					model(project, registryOf(LanguageSourceSet.class)).register(identifier.child(name), sourceSetType);
				}
			}
		});

		variants(project).configureEach(new WireParentSourceToSourceSetAction<>(SwiftSourceSetSpec.class, "swiftSources"));
	}
}
