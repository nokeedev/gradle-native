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
package dev.nokee.platform.swift.internal.plugins;

import dev.nokee.language.swift.internal.plugins.SwiftLanguageBasePlugin;
import dev.nokee.platform.nativebase.internal.plugins.NativePlatformPluginSupport;
import dev.nokee.platform.swift.internal.DefaultSwiftApplication;
import dev.nokee.utils.TextCaseUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

import static dev.nokee.platform.base.internal.BaseNameActions.baseName;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;

public class SwiftApplicationPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(SwiftCompilerPlugin.class);

		new NativePlatformPluginSupport<>()
			.useLanguagePlugin(SwiftLanguageBasePlugin.class)
			.registerComponent(DefaultSwiftApplication.class)
			.registerVariant(DefaultSwiftApplication.Variant.class)
			.registerAsMainComponent(baseName(convention(TextCaseUtils.toCamelCase(project.getName()))))
			.mountAsExtension()
			.execute(project);
	}
}
