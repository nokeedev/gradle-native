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

import dev.nokee.model.internal.ModelObjects;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.utils.Optionals;
import org.gradle.api.Named;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.nokee.utils.Optionals.safeAs;

public final class BaseNameConfigurationRule implements BiConsumer<ModelObjects.ModelObjectIdentity, HasBaseName> {
	private final ProviderFactory providers;

	public BaseNameConfigurationRule(ProviderFactory providers) {
		this.providers = providers;
	}

	@Override
	public void accept(ModelObjects.ModelObjectIdentity identity, HasBaseName target) {
		target.getBaseName().convention(providers.provider(() -> {
			return identity.getParents()
				.flatMap(projectionOf(HasBaseName.class))
				.map(toProviderOf(HasBaseName::getBaseName))
				.findFirst().orElseGet(this::absentProvider)
				.orElse(providers.provider(() -> {
					if (target instanceof Named) {
						return ((Named) target).getName();
					} else {
						return null;
					}
				}));
		}).flatMap(it -> it));
	}

	private <T> Provider<T> absentProvider() {
		return providers.provider(notDefined());
	}

	private static <V> Callable<V> notDefined() {
		return () -> null;
	}

	private static <T> Function<ModelObjects.ModelObjectIdentity, Stream<T>> projectionOf(Class<T> type) {
		return it -> {
			if (it.instanceOf(type)) {
				return Optionals.stream(it.getAsOptional().map(safeAs(type)));
			} else {
				return Stream.empty();
			}
		};
	}

	// Useful because the intention is to use the Provider type of a Property (for example)
	private static <T, U> Function<U, Provider<T>> toProviderOf(Function<? super U, ? extends Provider<T>> mapper) {
		return mapper::apply;
	}
}
