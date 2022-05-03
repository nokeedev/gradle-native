/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.model.internal.core;

import com.google.common.collect.ImmutableSet;
import dev.nokee.model.internal.type.ModelType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class NodeRegistrationFactories implements NodeRegistrationFactoryRegistry, NodeRegistrationFactoryLookup {
	private final Map<ModelType<?>, ModelRegistrationFactory> factories = new HashMap<>();

	@Override
	public <T> ModelRegistrationFactory get(ModelType<T> type) {
		return factories.get(type);
	}

	@Override
	public <T> void registerFactory(ModelType<T> type, ModelRegistrationFactory factory) {
		factories.put(type, factory);
	}

	@Override
	public Set<ModelType<?>> getSupportedTypes() {
		return ImmutableSet.copyOf(factories.keySet());
	}
}
