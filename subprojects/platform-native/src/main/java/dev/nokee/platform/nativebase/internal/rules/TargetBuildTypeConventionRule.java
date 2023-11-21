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

package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.model.internal.ModelObjects;
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.runtime.nativebase.internal.TargetBuildTypes;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import dev.nokee.utils.Optionals;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.nokee.utils.Optionals.safeAs;
import static java.util.Collections.singleton;

public final class TargetBuildTypeConventionRule implements BiConsumer<ModelObjects.ModelObjectIdentity, TargetBuildTypeAwareComponent> {
	private final ProviderFactory providers;

	public TargetBuildTypeConventionRule(ProviderFactory providers) {
		this.providers = providers;
	}

	@Override
	public void accept(ModelObjects.ModelObjectIdentity identity, TargetBuildTypeAwareComponent component) {
		component.getTargetBuildTypes().convention(providers.provider(() -> {
			return identity.getParents()
				.flatMap(projectionOf(TargetBuildTypeAwareComponent.class))
				.map(toProviderOf(TargetBuildTypeAwareComponent::getTargetBuildTypes))
				.findFirst()
				.orElseGet(this::absentProvider)
				.orElse(singleton(TargetBuildTypes.DEFAULT));
		}).flatMap(it -> it));
	}

	private <T> Provider<T> absentProvider() {
		return providers.provider(() -> null);
	}

	private static <T> Function<ModelObjects.ModelObjectIdentity, Stream<T>> projectionOf(Class<T> type) {
		return it -> Optionals.stream(it.getAsOptional().map(safeAs(type)));
	}

	// Useful because the intention is to use the Provider type of a Property (for example)
	private static <T, U> Function<U, Provider<T>> toProviderOf(Function<? super U, ? extends Provider<T>> mapper) {
		return mapper::apply;
	}
}
