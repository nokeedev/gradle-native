/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.nativebase.internal.archiving;

import dev.nokee.language.nativebase.internal.DefaultNativeToolChainSelector;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

public class NativeArchiveCapabilityPlugin<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	private final ProviderFactory providers;

	@Inject
	NativeArchiveCapabilityPlugin(ProviderFactory providers) {
		this.providers = providers;
	}

	@Override
	public void apply(T target) {
		val configurer = target.getExtensions().getByType(ModelConfigurer.class);
		configurer.configure(new OnDiscover(new NativeArchiveTaskRegistrationRule(target.getExtensions().getByType(ModelRegistry.class), new DefaultNativeToolChainSelector(((ProjectInternal) target).getModelRegistry(), providers))));
		configurer.configure(new ConfigureCreateTaskFromBaseNameRule(target.getExtensions().getByType(ModelRegistry.class)));
		configurer.configure(new ConfigureCreateTaskTargetPlatformFromBuildVariantRule(target.getExtensions().getByType(ModelRegistry.class)));
		configurer.configure(new AttachObjectFilesToCreateTaskRule(target.getExtensions().getByType(ModelRegistry.class)));
	}
}
