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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.core.DisplayName;
import dev.nokee.platform.base.DeclarableDependencyBucket;
import dev.nokee.platform.base.DependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;

import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.dependencyBuckets;

public abstract class DependencyBucketCapabilityPlugin<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	@Override
	public void apply(T target) {
		dependencyBuckets(target).configureEach(new DescriptionRule());
	}

	private static final class DescriptionRule implements Action<DependencyBucket> {
		@Override
		public void execute(DependencyBucket dependencyBucket) {
			if (dependencyBucket instanceof ModelElement) {
				final ModelObjectIdentifier identifier = ((ModelElement) dependencyBucket).getIdentifier();

				DisplayName displayName = null;
				if (dependencyBucket instanceof DeclarableDependencyBucket) {
					displayName = DisplayName.of(DependencyBuckets.defaultDisplayNameOfDeclarableBucket(identifier.getName()));
				} else {
					displayName = DisplayName.of(DependencyBuckets.defaultDisplayName(identifier.getName()));
				}

				dependencyBucket.getAsConfiguration().setDescription(DependencyBucketDescription.of(displayName).forOwner(dependencyBucket.toString()).toString());
			}
		}
	}
}
