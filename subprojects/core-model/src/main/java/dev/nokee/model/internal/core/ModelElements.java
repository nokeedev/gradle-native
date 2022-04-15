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
import dev.nokee.model.internal.ModelElementFactory;
import dev.nokee.model.internal.state.ModelState;
import lombok.val;

import java.util.List;

public final class ModelElements {
	public static ModelElement of(Object target) {
		val entity = ModelNodes.of(target);
		return entity.get(ModelElementFactory.class).createElement(entity);
	}

	public static ModelAction whenElementDiscovered(ModelAction action) {
		return new WhenElementDiscoveredAction(ModelActions.once(action));
	}

	private static final class WhenElementDiscoveredAction implements ModelAction, HasInputs {
		private final ModelAction delegate;
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		private WhenElementDiscoveredAction(ModelAction delegate) {
			this.delegate = delegate;
			val builder = ImmutableList.<ModelComponentReference<?>>builder();
			builder.add(ModelComponentReference.of(ModelState.IsAtLeastRegistered.class));
			if (delegate instanceof HasInputs) {
				builder.addAll(((HasInputs) delegate).getInputs());
			}
			this.inputs = builder.build();
			this.inputBits = inputs.stream().map(ModelComponentReference::componentBits).reduce(Bits.empty(), Bits::or);
		}

		@Override
		public List<? extends ModelComponentReference<?>> getInputs() {
			return inputs;
		}

		@Override
		public Bits getInputBits() {
			return inputBits;
		}

		@Override
		public void execute(ModelNode node) {
			if (node.getComponentBits().containsAll(inputBits)) {
				delegate.execute(node);
			}
		}
	}
}
