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
import dev.nokee.model.internal.ModelElement;
import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import org.gradle.api.reflect.TypeOf;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class ExtendsFromParentDependencyBucketAction<T> implements BiConsumer<ModelElement, DependencyAwareComponent<?>> {
	@SuppressWarnings({"unchecked", "UnstableApiUsage"})
	private Class<T> bucketUnderConfiguration() {
		return (Class<T>) new TypeToken<T>(getClass()) {}.getRawType();
	}

	@Override
	public final void accept(ModelElement identifier, DependencyAwareComponent<?> target) {
		final ComponentDependencies targetDependencies = target.getDependencies();
		if (bucketUnderConfiguration().isInstance(targetDependencies)) {
			identifier.getParents()
				.flatMap(projectionOf(new TypeOf<DependencyAwareComponent<?>>() {}))
				.findFirst()
				.map(DependencyAwareComponent::getDependencies)
				.filter(bucketUnderConfiguration()::isInstance)
				.ifPresent(parentDependencies -> {
					bucketOf(bucketUnderConfiguration().cast(targetDependencies)).extendsFrom(bucketOf(bucketUnderConfiguration().cast(parentDependencies)));
				});
		}
	}

	private static <T> Function<ModelElement, Stream<T>> projectionOf(TypeOf<T> type) {
		return it -> it.safeAs(type.getConcreteClass()).map(Stream::of).getOrElse(Stream.empty());
	}

	protected abstract DeclarableDependencyBucketSpec bucketOf(T dependencies);
}
