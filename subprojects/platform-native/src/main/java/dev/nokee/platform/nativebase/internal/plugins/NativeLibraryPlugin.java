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
package dev.nokee.platform.nativebase.internal.plugins;

import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibrary;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.platform.base.internal.BaseNameActions.baseName;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;

public class NativeLibraryPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		new NativePlatformPluginSupport<>()
			.useLanguagePlugin(CLanguageBasePlugin.class) // TODO: Should probably be NativeHeaderLanguageBasePlugin
			.registerComponent(DefaultNativeLibrary.class)
			.registerVariant(DefaultNativeLibrary.Variant.class)
			.registerAsMainComponent(baseName(convention(project.getName())))
			.mountAsExtension()
			.execute(project);
	}
}
