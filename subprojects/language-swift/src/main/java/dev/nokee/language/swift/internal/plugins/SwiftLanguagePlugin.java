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
package dev.nokee.language.swift.internal.plugins;

import dev.nokee.language.nativebase.internal.NativeLanguagePlugin;
import dev.nokee.language.nativebase.internal.NativeLanguageRegistrationFactory;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.registry.ModelLookup;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

public class SwiftLanguagePlugin implements Plugin<Project>, NativeLanguagePlugin {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(SwiftLanguageBasePlugin.class);
		project.getPluginManager().apply(SwiftCompilerPlugin.class);
		project.getExtensions().getByType(ModelLookup.class).get(ModelPath.root()).addComponentTag(SupportSwiftSourceSetTag.class);
	}

	@Override
	public Class<? extends NativeLanguageRegistrationFactory> getRegistrationFactoryType() {
		return SwiftLanguageBasePlugin.DefaultSwiftSourceSetRegistrationFactory.class;
	}
}
