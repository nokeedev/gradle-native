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
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.DeclarableDependencyBucket;
import dev.nokee.utils.ActionUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

import static dev.nokee.platform.base.internal.dependencies.DependencyBuckets.assertConfigurableNotation;
import static dev.nokee.utils.ProviderUtils.finalizeValue;

public /*final*/ abstract class DeclarableDependencyBucketSpec extends ModelElementSupport implements DeclarableDependencyBucket
	, DependencyBucketMixIn
{
	private final DependencyFactory dependencyFactory;

	@Inject
	public DeclarableDependencyBucketSpec(DependencyHandler handler, ModelObjectRegistry<Configuration> configurationRegistry) {
		getExtensions().add("$configuration", configurationRegistry.register(getIdentifier(), Configuration.class).get());

		this.dependencyFactory = DependencyFactory.forHandler(handler);
	}

	// We can't realistically delay until realize because Kotlin plugin suck big time and Gradle removed important APIs... Too bad, blame Gradle or Kotlin.
	@Override
	public void addDependency(Object notation) {
		getAsConfiguration().getDependencies().addLater(create(new DependencyElement(notation)));
	}

	@Override
	public void addDependency(Object notation, Action<? super ModuleDependency> action) {
		getAsConfiguration().getDependencies().addLater(create(new DependencyElement(assertConfigurableNotation(notation), action)));
	}

	private Provider<Dependency> create(DependencyElement element) {
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
						return dependencyFactory.create(((Provider<?>) notation).get());
					} else {
						return dependencyFactory.create(notation);
					}
				}
			});
		});
	}

	private ActionUtils.Action<Dependency> defaultAction() {
		final Action<ModuleDependency> action = finalizeValue(getDefaultDependencyAction()).map(ActionUtils.Action::of).getOrElse(ActionUtils.doNothing());
		return dependency -> {
			if (dependency instanceof ModuleDependency) {
				action.execute((ModuleDependency) dependency);
			} else {
				// ignores
			}
		};
	}

	@Override
	public String toString() {
		return "declarable dependency bucket '" + getName() + "'";
	}
}
