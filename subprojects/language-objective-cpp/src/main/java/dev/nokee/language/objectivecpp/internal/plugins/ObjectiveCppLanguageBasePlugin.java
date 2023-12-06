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
package dev.nokee.language.objectivecpp.internal.plugins;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.PropertySpec;
import dev.nokee.language.base.internal.SourceProperty;
import dev.nokee.language.nativebase.internal.plugins.LanguageNativeBasePlugin;
import dev.nokee.language.nativebase.internal.plugins.NativeHeaderLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetSpec;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Named;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.mapOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.util.internal.NotPredicate.not;

public class ObjectiveCppLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(NativeHeaderLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(ObjectiveCppSourceSetSpec.class);

		DefaultImporter.forProject(project)
			.defaultImport(ObjectiveCppSourceSet.class);

		// No need to register anything as ObjectiveCSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.

		model(project, mapOf(PropertySpec.class)).configureEach(SourceProperty.class, it -> {
			if (it.getIdentifier().getName().toString().equals("objectiveCppSources")) {
				it.getParents().findFirst().map(Named::getName).filter(not(String::isEmpty)).ifPresent(name -> {
					it.getSource().from(it.getSourceName().map(sourceName -> String.format("src/%s/objcpp", name, sourceName)));
				});
			}
		});
	}
}
