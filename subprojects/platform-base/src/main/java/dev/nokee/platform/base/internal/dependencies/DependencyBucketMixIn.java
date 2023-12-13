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

import dev.nokee.platform.base.DependencyBucket;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderConvertible;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.Set;

interface DependencyBucketMixIn extends DependencyBucketInternal, ExtensionAware {
	@Inject
	ProviderFactory getProviders();

	@Inject
	ObjectFactory getObjects();

	@Override
	@SuppressWarnings("unchecked")
	default DependencyBucketInternal extendsFrom(Object... buckets) {
		for (Object bucket : buckets) {
			Configuration parentConfiguration = null;
			if (bucket instanceof DependencyBucket) {
				parentConfiguration = ((DependencyBucket) bucket).getAsConfiguration();
			} else if (bucket instanceof ProviderConvertible) {
				parentConfiguration = ((ProviderConvertible<DependencyBucket>) bucket).asProvider().get().getAsConfiguration();
			} else if (bucket instanceof Provider) {
				parentConfiguration = ((Provider<DependencyBucket>) bucket).get().getAsConfiguration();
			} else {
				throw new UnsupportedOperationException("only accept DependencyBucket, ProviderConvertible<DependencyBucket> and Provider<DependencyBucket>");
			}

			// Avoid cyclic extendsFrom
			if (!getAsConfiguration().equals(parentConfiguration)) {
				getAsConfiguration().extendsFrom(parentConfiguration);

				// For discovery
				getAsConfiguration().getDependencies().addAllLater(getObjects().listProperty(Dependency.class).value(parentConfiguration.getDependencies()));
			}
		}
		return this;
	}

	default Provider<Set<Dependency>> getDependencies() {
		return getProviders().provider(() -> getAsConfiguration().getDependencies());
	}

	@Override
	default Configuration getAsConfiguration() {
		return getExtensions().getByType(Configuration.class);
	}
}
