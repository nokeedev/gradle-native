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

import dev.nokee.language.base.internal.LanguageSourceSetRegistrationFactory;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.c.internal.plugins.CHeaderSetRegistrationFactory;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.nativebase.internal.*;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ObjectiveCLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(NativeHeaderLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		DefaultImporter.forProject(project)
			.defaultImport(ObjectiveCSourceSet.class)
			.defaultImport(CHeaderSet.class)
			.defaultImport(NativeHeaderSet.class);

		// No need to register anything as ObjectiveCSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.

		project.getExtensions().add("__nokee_objectiveCHeaderSetFactory", new CHeaderSetRegistrationFactory(project.getExtensions().getByType(LanguageSourceSetRegistrationFactory.class)));
		project.getExtensions().add("__nokee_objectiveCSourceSetFactory", new ObjectiveCSourceSetRegistrationFactory(
			project.getExtensions().getByType(LanguageSourceSetRegistrationFactory.class),
			project.getExtensions().getByType(HeadersPropertyRegistrationActionFactory.class),
			project.getExtensions().getByType(HeaderSearchPathsConfigurationRegistrationActionFactory.class),
			project.getExtensions().getByType(NativeCompileTaskRegistrationActionFactory.class)
		));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new RegisterObjectiveCSourceSetProjectionRule.LegacySourceSetRule(project.getObjects()));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new RegisterObjectiveCSourceSetProjectionRule.DefaultSourceSetRule(project.getObjects()));
	}
}
