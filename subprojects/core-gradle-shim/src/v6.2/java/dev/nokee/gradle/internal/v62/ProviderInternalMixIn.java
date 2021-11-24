/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.gradle.internal.v62;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.internal.provider.ValueSanitizer;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.provider.Provider;
import org.gradle.internal.DisplayName;

import javax.annotation.Nullable;

@SuppressWarnings("UnstableApiUsage")
interface ProviderInternalMixIn<T> extends ProviderInternal<T> {
	ProviderInternal<T> getDelegate();

	@Override
	default T get() {
		return getDelegate().get();
	}

	@Nullable
	@Override
	default T getOrNull() {
		return getDelegate().getOrNull();
	}

	@Override
	default T getOrElse(T defaultValue) {
		return getDelegate().getOrElse(defaultValue);
	}

	@Override
	default <S> ProviderInternal<S> map(Transformer<? extends S, ? super T> transformer) {
		return getDelegate().map(transformer);
	}

	@Override
	default Value<? extends T> calculateValue() {
		return getDelegate().calculateValue();
	}

	@Override
	default <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
		return getDelegate().flatMap(transformer);
	}

	@Override
	default boolean maybeVisitBuildDependencies(TaskDependencyResolveContext context) {
		return getDelegate().maybeVisitBuildDependencies(context);
	}

	@Override
	default void visitProducerTasks(Action<? super Task> visitor) {
		getDelegate().visitProducerTasks(visitor);
	}

	@Override
	default boolean isValueProducedByTask() {
		return getDelegate().isValueProducedByTask();
	}

	@Override
	default boolean isPresent() {
		return getDelegate().isPresent();
	}

	@Override
	default Provider<T> orElse(T value) {
		return getDelegate().orElse(value);
	}

	@Override
	default Provider<T> orElse(Provider<? extends T> provider) {
		return getDelegate().orElse(provider);
	}

	@Override
	default ProviderInternal<T> asSupplier(DisplayName owner, Class<? super T> targetType, ValueSanitizer<? super T> sanitizer) {
		return getDelegate().asSupplier(owner, targetType, sanitizer);
	}

	@Override
	default ProviderInternal<T> withFinalValue() {
		return getDelegate().withFinalValue();
	}

	@Override
	default void visitDependencies(TaskDependencyResolveContext context) {
		getDelegate().visitDependencies(context);
	}
}
