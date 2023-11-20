/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.platform.base.internal.rules;

import com.google.common.reflect.TypeToken;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjects;
import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.base.internal.mixins.ImplementationDependencyBucketMixIn;

import java.util.function.BiConsumer;

public abstract class ExtendsFromImplementationDependencyBucketAction<T> implements BiConsumer<ModelObjects.ModelObjectIdentity, DependencyAwareComponent<?>> {
	@SuppressWarnings({"unchecked", "UnstableApiUsage"})
	private Class<T> bucketUnderConfiguration() {
		return (Class<T>) new TypeToken<T>(getClass()) {}.getRawType();
	}

	@Override
	public final void accept(ModelObjects.ModelObjectIdentity identifier, DependencyAwareComponent<?> target) {
		final ComponentDependencies targetDependencies = target.getDependencies();
		if (targetDependencies instanceof ImplementationDependencyBucketMixIn && bucketUnderConfiguration().isInstance(targetDependencies)) {
			bucketOf(bucketUnderConfiguration().cast(targetDependencies)).extendsFrom(((ImplementationDependencyBucketMixIn) targetDependencies).getImplementation());
		}
	}

	protected abstract DeclarableDependencyBucketSpec bucketOf(T dependencies);
}
