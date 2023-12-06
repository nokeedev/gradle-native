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
import dev.nokee.language.c.internal.CSourceSetSpec;
import dev.nokee.language.nativebase.internal.plugins.LanguageNativeBasePlugin;
import dev.nokee.language.nativebase.internal.plugins.NativeHeaderLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;

public class CLanguageBasePlugin implements Plugin<Project> {
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
	}
}
