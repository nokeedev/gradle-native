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
package dev.nokee.platform.nativebase.internal.linking;

import dev.nokee.language.nativebase.internal.DefaultNativeToolChainSelector;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.TaskRegistrationFactory;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.nativebase.internal.AttachAttributesToConfigurationRule;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

public class NativeLinkCapabilityPlugin<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	private final ObjectFactory objects;
	private final ProviderFactory providers;

	@Inject
	public NativeLinkCapabilityPlugin(ObjectFactory objects, ProviderFactory providers) {
		this.objects = objects;
		this.providers = providers;
	}

	@Override
	public void apply(T target) {
		val configurer = target.getExtensions().getByType(ModelConfigurer.class);
		configurer.configure(new AttachAttributesToConfigurationRule<>(LinkLibrariesConfiguration.class, target.getExtensions().getByType(ModelRegistry.class), objects));
		configurer.configure(new OnDiscover(new LinkLibrariesConfigurationRegistrationRule(target.getExtensions().getByType(ModelRegistry.class), objects)));
		configurer.configure(new OnDiscover(new NativeLinkTaskRegistrationRule(target.getExtensions().getByType(ModelRegistry.class), target.getExtensions().getByType(TaskRegistrationFactory.class), new DefaultNativeToolChainSelector(((ProjectInternal) target).getModelRegistry(), providers))));
		configurer.configure(new AttachLinkLibrariesToLinkTaskRule(target.getExtensions().getByType(ModelRegistry.class)));
		configurer.configure(new ConfigureLinkTaskFromBaseNameRule(target.getExtensions().getByType(ModelRegistry.class)));
		configurer.configure(new AttachObjectFilesToLinkTaskRule(target.getExtensions().getByType(ModelRegistry.class)));
		configurer.configure(new ConfigureLinkTaskDefaultsRule(target.getExtensions().getByType(ModelRegistry.class)));
		configurer.configure(new ConfigureLinkTaskTargetPlatformFromBuildVariantRule(target.getExtensions().getByType(ModelRegistry.class)));
		configurer.configure(new ConfigureLinkTaskBundleRule(target.getExtensions().getByType(ModelRegistry.class)));
	}
}
