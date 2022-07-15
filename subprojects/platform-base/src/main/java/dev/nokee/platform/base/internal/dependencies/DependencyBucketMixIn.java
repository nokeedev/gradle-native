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

import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.provider.Provider;

import java.util.Set;

import static dev.nokee.model.internal.core.ModelProperties.add;
import static dev.nokee.platform.base.internal.dependencies.DependencyBuckets.assertConfigurableNotation;

interface DependencyBucketMixIn extends DependencyBucket, ModelBackedNamedMixIn {
	default void addDependency(Object notation) {
		val entity = ModelNodes.of(this).get(BucketDependenciesProperty.class).get();
		add(entity, new DependencyElement(notation));
	}

	default void addDependency(Object notation, Action<? super ModuleDependency> action) {
		val entity = ModelNodes.of(this).get(BucketDependenciesProperty.class).get();
		add(entity, new DependencyElement(assertConfigurableNotation(notation), action));
	}

	default Provider<Set<Dependency>> getDependencies() {
		return ProviderUtils.supplied(() -> ModelStates.finalize(ModelNodes.of(this)).get(BucketDependencies.class).get());
	}

	@Override
	default Configuration getAsConfiguration() {
		return ModelNodeUtils.get(ModelNodes.of(this), Configuration.class);
	}
}
