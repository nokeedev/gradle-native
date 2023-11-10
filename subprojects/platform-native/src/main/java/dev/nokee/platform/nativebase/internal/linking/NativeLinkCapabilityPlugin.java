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
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.nativebase.HasLinkTask;
import dev.nokee.platform.nativebase.internal.AttachAttributesToConfigurationRule;
import dev.nokee.utils.TaskUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Task;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.mapOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.artifacts;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;

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
		variants(target).configureEach(new AttachAttributesToConfigurationRule(HasLinkLibrariesDependencyBucket.class, HasLinkLibrariesDependencyBucket::getLinkLibraries, objects, model(target, mapOf(Artifact.class))));
		artifacts(target).configureEach(new LinkLibrariesConfigurationRegistrationRule(objects, model(target, objects())));
		artifacts(target).configureEach(new NativeLinkTaskRegistrationRule(new DefaultNativeToolChainSelector(((ProjectInternal) target).getModelRegistry(), providers)));
		artifacts(target).configureEach(new ConfigureLinkTaskFromBaseNameRule());
		configurer.configure(new AttachObjectFilesToLinkTaskRule());
		artifacts(target).configureEach(new ConfigureLinkTaskDefaultsRule());
		variants(target).configureEach(new ConfigureLinkTaskTargetPlatformFromBuildVariantRule(model(target, mapOf(Task.class))));
		artifacts(target).configureEach(new ConfigureLinkTaskBundleRule());
		artifacts(target).configureEach(new ConfigureLinkTaskDescriptionRule());
	}

	private static final class ConfigureLinkTaskDescriptionRule implements Action<Artifact> {
		@Override
		public void execute(Artifact target) {
			if (target instanceof HasLinkTask) {
				((HasLinkTask<?>) target).getLinkTask().configure(TaskUtils.configureDescription("Links the %s.", target));
			}
		}
	}
}
