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
package dev.nokee.model.internal.registry;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.internal.provider.ValueSanitizer;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.provider.Provider;
import org.gradle.internal.DisplayName;

import javax.annotation.Nullable;

public class ModelBackedNamedDomainObjectProvider<T> implements NamedDomainObjectProvider<T>, ProviderInternal<T> {
	private final ProviderInternal<T> provider;
	private final ModelNodeBackedProvider<T> delegate;

	public ModelBackedNamedDomainObjectProvider(ModelNodeBackedProvider<T> delegate) {
		this.provider = (ProviderInternal<T>) delegate.getAsProvider();
		this.delegate = delegate;
	}

	@Override
	public void configure(Action<? super T> action) {
		delegate.configure(action);
	}

	@Override
	public String getName() {
		return delegate.getIdentifier().getPath().getName();
	}


	@Nullable
	@Override
	public Class<T> getType() {
		return provider.getType();
	}

	@Override
	public <S> ProviderInternal<S> map(Transformer<? extends S, ? super T> transformer) {
		return provider.map(transformer);
	}

	@Override
	public Value<? extends T> calculateValue() {
		return provider.calculateValue();
	}

	@Override
	public ProviderInternal<T> asSupplier(DisplayName owner, Class<? super T> targetType, ValueSanitizer<? super T> sanitizer) {
		return provider.asSupplier(owner, targetType, sanitizer);
	}

	@Override
	public ProviderInternal<T> withFinalValue() {
		return provider.withFinalValue();
	}

	@Override
	public boolean maybeVisitBuildDependencies(TaskDependencyResolveContext context) {
		return provider.maybeVisitBuildDependencies(context);
	}

	@Override
	public void visitProducerTasks(Action<? super Task> visitor) {
		provider.visitProducerTasks(visitor);
	}

	@Override
	public boolean isValueProducedByTask() {
		return provider.isValueProducedByTask();
	}

	@Override
	public void visitDependencies(TaskDependencyResolveContext context) {
		provider.visitDependencies(context);
	}

	@Override
	public T get() {
		return provider.get();
	}

	@Nullable
	@Override
	public T getOrNull() {
		return provider.getOrNull();
	}

	@Override
	public T getOrElse(T defaultValue) {
		return provider.getOrElse(defaultValue);
	}

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
		return provider.flatMap(transformer);
	}

	@Override
	public boolean isPresent() {
		return provider.isPresent();
	}

	@Override
	public Provider<T> orElse(T value) {
		return provider.orElse(value);
	}

	@Override
	public Provider<T> orElse(Provider<? extends T> provider) {
		return this.provider.orElse(provider);
	}
}
