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
package dev.nokee.model.internal.core;

import dev.nokee.internal.reflect.Instantiator;
import dev.nokee.model.internal.registry.ManagedModelProjection;

import static java.util.Objects.requireNonNull;

public final class BindManagedProjectionService implements ModelComponent {
	private final Instantiator instantiator;

	public BindManagedProjectionService(Instantiator instantiator) {
		this.instantiator = requireNonNull(instantiator);
	}

	public ModelProjection bindManagedProjectionWithInstantiator(ModelProjection projection) {
		if (projection instanceof ManagedModelProjection) {
			return ((ManagedModelProjection<?>) projection).bind(instantiator);
		}
		return projection;
	}
}
