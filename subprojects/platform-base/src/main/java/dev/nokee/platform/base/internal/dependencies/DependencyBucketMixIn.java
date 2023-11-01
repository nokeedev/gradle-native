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

import dev.nokee.model.DependencyFactory;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.provider.ProviderConvertible;
import dev.nokee.utils.ActionUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.Set;

import static dev.nokee.platform.base.internal.dependencies.DependencyBuckets.assertConfigurableNotation;
import static dev.nokee.utils.ProviderUtils.finalizeValue;

interface DependencyBucketMixIn extends DependencyBucketInternal {
	@Inject
	ProviderFactory getProviders();

	DependencyFactory getDependencyFactory();

	@Override
	@SuppressWarnings("unchecked")
	default DependencyBucketInternal extendsFrom(Object... buckets) {
		for (Object bucket : buckets) {
			if (bucket instanceof DependencyBucket) {
				getAsConfiguration().extendsFrom(((DependencyBucket) bucket).getAsConfiguration());
			} else if (bucket instanceof ProviderConvertible) {
				getAsConfiguration().extendsFrom(((ProviderConvertible<DependencyBucket>) bucket).asProvider().get().getAsConfiguration());
			} else if (bucket instanceof Provider) {
				getAsConfiguration().extendsFrom(((Provider<DependencyBucket>) bucket).get().getAsConfiguration());
			} else {
				throw new UnsupportedOperationException("only accept DependencyBucket, ProviderConvertible<DependencyBucket> and Provider<DependencyBucket>");
			}
		}
		return this;
	}

	// We can't realistically delay until realize because Kotlin plugin suck big time and Gradle removed important APIs... Too bad, blame Gradle or Kotlin.
	default void addDependency(Object notation) {
		getAsConfiguration().getDependencies().addLater(create(new DependencyElement(notation)));
	}

	default void addDependency(Object notation, Action<? super ModuleDependency> action) {
		getAsConfiguration().getDependencies().addLater(create(new DependencyElement(assertConfigurableNotation(notation), action)));
	}

	default Provider<Dependency> create(DependencyElement element) {
		return getProviders().provider(() -> {
			return element.resolve(new DependencyFactory() {
				private final Action<Dependency> action = defaultAction();

				@Override
				public Dependency create(Object notation) {
					val result = toDependency(notation);
					action.execute(result);
					return result;
				}

				private Dependency toDependency(Object notation) {
					if (notation instanceof Provider) {
						return getDependencyFactory().create(((Provider<?>) notation).get());
					} else {
						return getDependencyFactory().create(notation);
					}
				}
			});
		});
	}

	default ActionUtils.Action<Dependency> defaultAction() {
		final Action<ModuleDependency> action = finalizeValue(getDefaultDependencyAction()).map(ActionUtils.Action::of).getOrElse(ActionUtils.doNothing());
		return dependency -> {
			if (dependency instanceof ModuleDependency) {
				action.execute((ModuleDependency) dependency);
			} else {
				// ignores
			}
		};
	}

	default Provider<Set<Dependency>> getDependencies() {
		return getProviders().provider(() -> getAsConfiguration().getDependencies());
	}

	@Override
	default Configuration getAsConfiguration() {
		return getExtensions().getByType(Configuration.class);
	}
}
