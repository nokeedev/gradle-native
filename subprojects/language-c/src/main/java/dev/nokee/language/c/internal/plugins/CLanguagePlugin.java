/*
 * Copyright 2020-2021 the original author or authors.
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

import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.internal.CSourceSetExtensible;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.internal.registry.ModelConfigurer;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.language.base.internal.SourceSetExtensible.discoveringInstanceOf;
import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.internal.core.ModelActions.*;

public class CLanguagePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(CLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		val modelConfigurer = project.getExtensions().getByType(ModelConfigurer.class);
		modelConfigurer.configure(matching(discoveringInstanceOf(CSourceSetExtensible.class),
			once(register(sourceSet("c", CSourceSet.class)))));
	}
}
