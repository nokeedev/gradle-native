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
import dev.nokee.language.base.internal.PropertySpec;
import dev.nokee.language.base.internal.SourceProperty;
import dev.nokee.language.base.internal.SourcePropertyName;
import dev.nokee.language.nativebase.internal.LanguageNativeBasePlugin;
import dev.nokee.language.nativebase.internal.NativeHeaderLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.WireParentSourceToSourceSetAction;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Named;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.mapOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;
import static dev.nokee.util.internal.NotPredicate.not;

public class ObjectiveCLanguageBasePlugin implements Plugin<Project> {
	public static final SourcePropertyName OBJECTIVE_C_SOURCES = () -> "objectiveCSources";

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(NativeHeaderLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(ObjectiveCSourceSetSpec.class);

		DefaultImporter.forProject(project)
			.defaultImport(ObjectiveCSourceSet.class);

		// No need to register anything as ObjectiveCSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.

		variants(project).configureEach(new WireParentSourceToSourceSetAction<>(ObjectiveCSourceSetSpec.class, OBJECTIVE_C_SOURCES));

		model(project, mapOf(PropertySpec.class)).configureEach(SourceProperty.class, it -> {
			if (it.getIdentifier().getName().toString().equals("objectiveCSources")) {
				it.getParents().findFirst().map(Named::getName).filter(not(String::isEmpty)).ifPresent(name -> {
					it.getSource().from(it.getSourceName().map(sourceName -> String.format("src/%s/objc", name, sourceName)));
				});
			}
		});
	}
}
