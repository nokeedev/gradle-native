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
package dev.nokee.gradle;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.provider.Provider;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class NamedDomainObjectProviderSpec<T> {
	private final Supplier<String> nameSupplier;
	private final Supplier<Class<T>> typeSupplier;
	private final Provider<T> delegate;
	private final Consumer<? super Action<? super T>> configurer;

	public NamedDomainObjectProviderSpec(Supplier<String> nameSupplier, Supplier<Class<T>> typeSupplier, Provider<T> delegate, Consumer<? super Action<? super T>> configurer) {
		this.nameSupplier = nameSupplier;
		this.typeSupplier = typeSupplier;
		this.delegate = delegate;
		this.configurer = configurer;
	}

	public String getName() {
		return nameSupplier.get();
	}

	public Class<T> getType() {
		return typeSupplier.get();
	}

	public Provider<T> getDelegate() {
		return delegate;
	}

	public Consumer<? super Action<? super T>> getConfigurer() {
		return configurer;
	}

	public static <T> NamedDomainObjectProviderSpec<T> of(NamedDomainObjectProvider<T> provider) {
		@SuppressWarnings("unchecked")
		ProviderInternal<T> delegate = (ProviderInternal<T>) provider;
		return new NamedDomainObjectProviderSpec<T>(provider::getName, delegate::getType, delegate, provider::configure);
	}

	public static Builder<?> builder() {
		return new Builder<>();
	}

	public static final class Builder<T> {
		private Supplier<String> nameSupplier;
		private Supplier<Class<T>> typeSupplier;
		private Provider<T> delegate;
		private Consumer<? super Action<? super T>> configurer;

		public Builder<T> named(String name) {
			this.nameSupplier = () -> name;
			return this;
		}

		public Builder<T> named(Supplier<String> nameSupplier) {
			this.nameSupplier = nameSupplier;
			return this;
		}

		@SuppressWarnings("unchecked")
		public <S> Builder<S> delegateTo(Provider<S> delegate) {
			((Builder<S>) this).delegate = delegate;
			return (Builder<S>) this;
		}

		@SuppressWarnings("unchecked")
		public <S> Builder<S> typedAs(Class<S> type) {
			((Builder<S>) this).typeSupplier = () -> type;
			return (Builder<S>) this;
		}

		public Builder<T> configureUsing(Consumer<? super Action<? super T>> configurer) {
			this.configurer = configurer;
			return this;
		}

		public NamedDomainObjectProviderSpec<T> build() {
			if (nameSupplier == null && delegate instanceof NamedDomainObjectProvider) {
				nameSupplier = ((NamedDomainObjectProvider<T>) delegate)::getName;
			}

			if (configurer == null) {
				if (delegate instanceof NamedDomainObjectProvider) {
					configurer = ((NamedDomainObjectProvider<T>) delegate)::configure;
				} else {
					configurer = action -> { throw new UnsupportedOperationException(); };
				}
			}

			if (typeSupplier == null && delegate instanceof ProviderInternal) {
				typeSupplier = ((ProviderInternal<T>) delegate)::getType;
			}

			return new NamedDomainObjectProviderSpec<>(nameSupplier, typeSupplier, delegate, configurer);
		}
	}
}
