/*
 * Copyright 2020-2021 the original author or authors.
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
import lombok.EqualsAndHashCode;
import lombok.val;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public final class ModelActions {
	private ModelActions() {}

	public static ModelAction doNothing() {
		return ModelNodeAction.DO_NOTHING;
	}

	private enum ModelNodeAction implements ModelAction {
		DO_NOTHING {
			@Override
			public void execute(ModelNode node) {
				// do nothing.
			}

			@Override
			public String toString() {
				return "ModelActions.doNothing()";
			}
		}
	}

	/**
	 * Returns an action that will only be executed once regardless of the node.
	 *
	 * @param action  the action to execute only once
	 * @return an action that will only execute once regardless of the node, never null.
	 */
	public static ModelAction once(ModelAction action) {
		return new OnceModelAction(action);
	}

	@EqualsAndHashCode
	private static final class OnceModelAction implements ModelAction, HasInputs {
		private final ModelAction action;
		@EqualsAndHashCode.Exclude private final Set<ModelEntityId> alreadyExecuted = new HashSet<>();
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		public OnceModelAction(ModelAction action) {
			this.action = requireNonNull(action);
			val builder = ImmutableList.<ModelComponentReference<?>>builder();
			if (action instanceof HasInputs) {
				builder.addAll(((HasInputs) action).getInputs());
			}
			this.inputs = builder.build();
			this.inputBits = inputs.stream().map(ModelComponentReference::componentBits).reduce(Bits.empty(), Bits::or);
		}

		@Override
		public void execute(ModelNode node) {
			if (node.getComponentBits().containsAll(inputBits)) {
				if (alreadyExecuted.add(node.getId())) {
					action.execute(node);
				}
			}
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
		public String toString() {
			return "ModelActions.once(" + action + ")";
		}
	}
}
