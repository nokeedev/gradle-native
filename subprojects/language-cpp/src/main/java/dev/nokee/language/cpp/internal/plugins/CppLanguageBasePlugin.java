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

import dev.nokee.language.base.internal.LanguageSourceSetRegistrationFactory;
import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.nativebase.internal.*;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.scripts.DefaultImporter;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class CppLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(NativeHeaderLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		DefaultImporter.forProject(project)
			.defaultImport(NativeHeaderSet.class)
			.defaultImport(CppHeaderSet.class)
			.defaultImport(CppSourceSet.class);

		// No need to register anything as CppHeaderSet and CppSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.

		project.getExtensions().add("__nokee_cppHeaderSetFactory", new CppHeaderSetRegistrationFactory(project.getExtensions().getByType(LanguageSourceSetRegistrationFactory.class)));
		project.getExtensions().add("__nokee_cppSourceSetFactory", new CppSourceSetRegistrationFactory(
			project.getExtensions().getByType(LanguageSourceSetRegistrationFactory.class),
			project.getExtensions().getByType(HeadersPropertyRegistrationActionFactory.class),
			project.getExtensions().getByType(HeaderSearchPathsConfigurationRegistrationActionFactory.class),
			project.getExtensions().getByType(NativeCompileTaskRegistrationActionFactory.class),
			project.getObjects()
		));
	}
}
