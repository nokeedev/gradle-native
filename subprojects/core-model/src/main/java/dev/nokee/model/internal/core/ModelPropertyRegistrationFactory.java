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

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;

public final class ModelPropertyRegistrationFactory {
	private final ModelLookup lookup;

	public ModelPropertyRegistrationFactory(ModelLookup lookup) {
		this.lookup = lookup;
	}

	public ModelRegistration<?> create(ModelPath path, ModelNode entity) {
		return ModelRegistration.builder()
			.withPath(path)
			.action(ModelActionWithInputs.of(ModelType.of(ModelPath.class), ModelType.of(ModelState.IsAtLeastRealized.class), (e, p, ignored) -> {
				if (p.equals(path)) {
					ModelStates.realize(entity);
				} else if (p.equals(ModelNodeUtils.getPath(entity))) {
					ModelStates.realize(lookup.get(path));
				}
			}))
			.action(ModelActionWithInputs.of(ModelType.of(ModelPath.class), ModelType.of(ModelState.IsAtLeastCreated.class), (e, p, ignored) -> {
				if (p.equals(path)) {
					e.addComponent(new ModelProjection() {
						private final ModelNode delegate = entity;

						@Override
						public <T> boolean canBeViewedAs(ModelType<T> type) {
							return ModelNodeUtils.canBeViewedAs(delegate, type);
						}

						@Override
						public <T> T get(ModelType<T> type) {
							return ModelNodeUtils.get(delegate, type);
						}

						@Override
						public Iterable<String> getTypeDescriptions() {
							return ImmutableList.of(ModelNodeUtils.getTypeDescription(delegate).orElse("<unknown>"));
						}
					});
				}
			}))
			.build();
	}
}
