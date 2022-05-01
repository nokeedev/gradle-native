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
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.DefaultKnownDomainObject;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Action;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.nokee.model.internal.core.ModelComponentReference.ofProjection;
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

	public static <T> ModelAction executeUsingProjection(ModelType<T> type, Action<? super T> action) {
		return new ExecuteUsingProjectionModelAction<>(type, action);
	}

	@EqualsAndHashCode
	private static final class ExecuteUsingProjectionModelAction<T> implements ModelAction, HasInputs {
		private final ModelType<T> type;
		private final Action<? super T> action;
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		private ExecuteUsingProjectionModelAction(ModelType<T> type, Action<? super T> action) {
			this.type = requireNonNull(type);
			this.action = requireNonNull(action);
			this.inputs = ImmutableList.of(ofProjection(type.getConcreteType()));
			this.inputBits = inputs.stream().map(ModelComponentReference::componentBits).reduce(Bits.empty(), Bits::or);
		}

		@Override
		public void execute(ModelNode node) {
			if (node.getComponentBits().containsAll(inputBits)) {
				action.execute(ModelNodeUtils.get(node, type));
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
			return "ModelActions.executeUsingProjection(" + type + ", " + action + ")";
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
		@EqualsAndHashCode.Exclude private final Set<ModelPath> alreadyExecuted = new HashSet<>();
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		public OnceModelAction(ModelAction action) {
			this.action = requireNonNull(action);
			val builder = ImmutableList.<ModelComponentReference<?>>builder();
			builder.add(ModelComponentReference.of(ModelPathComponent.class));
			if (action instanceof HasInputs) {
				builder.addAll(((HasInputs) action).getInputs());
			}
			this.inputs = builder.build();
			this.inputBits = inputs.stream().map(ModelComponentReference::componentBits).reduce(Bits.empty(), Bits::or);
		}

		@Override
		public void execute(ModelNode node) {
			if (node.getComponentBits().containsAll(inputBits)) {
				if (alreadyExecuted.add(ModelNodeUtils.getPath(node))) {
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

	/**
	 * Returns an action that will execute only if the specified predicate matches.
	 *
	 * @param spec  the predicate to match
	 * @param action  the action to execute
	 * @return an action that will execute the specified action only for the matching predicate, never null.
	 */
	public static ModelAction matching(ModelSpec spec, ModelAction action) {
		return new MatchingModelAction(spec, action);
	}

	@EqualsAndHashCode
	private static final class MatchingModelAction implements ModelAction, HasInputs {
		private final ModelSpec spec;
		private final ModelAction action;
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		public MatchingModelAction(ModelSpec spec, ModelAction action) {
			this.spec = requireNonNull(spec);
			this.action = requireNonNull(action);

			val builder = ImmutableList.<ModelComponentReference<?>>builder();
			if (spec instanceof HasInputs) {
				builder.addAll(((HasInputs) spec).getInputs());
			}
			if (action instanceof HasInputs) {
				builder.addAll(((HasInputs) action).getInputs());
			}
			this.inputs = builder.build();
			this.inputBits = inputs.stream().map(ModelComponentReference::componentBits).reduce(Bits.empty(), Bits::or);
		}

		@Override
		public void execute(ModelNode node) {
			if (node.getComponentBits().containsAll(inputBits)) {
				if (spec.isSatisfiedBy(node)) {
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
			return "ModelActions.matching(" + spec + ", " + action + ")";
		}
	}

	/**
	 * Returns an action that will execute the specified action using as a known projection type.
	 *
	 * @param type  the projection type
	 * @param action  the action to execute
	 * @param <T>  the projection type
	 * @return an action that will execute for known domain object, never null.
	 */
	public static <T> ModelAction executeAsKnownProjection(ModelType<T> type, Action<? super KnownDomainObject<T>> action) {
		return new ExecuteAsKnownProjectionModelAction<>(type, action);
	}

	// TODO: Should we also ensure the node is at least registered (or discovered)?
	@EqualsAndHashCode
	private static class ExecuteAsKnownProjectionModelAction<T> implements ModelAction, HasInputs {
		private final ModelType<T> type;
		private final Action<? super KnownDomainObject<T>> action;
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		public ExecuteAsKnownProjectionModelAction(ModelType<T> type, Action<? super KnownDomainObject<T>> action) {
			this.type = requireNonNull(type);
			this.action = requireNonNull(action);
			this.inputs = ImmutableList.of(ofProjection(type.getConcreteType()));
			this.inputBits = inputs.stream().map(ModelComponentReference::componentBits).reduce(Bits.empty(), Bits::or);
		}

		@Override
		public void execute(ModelNode node) {
			if (node.getComponentBits().containsAll(inputBits)) {
				action.execute(DefaultKnownDomainObject.of(type, node));
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
			return "ModelActions.executeAsKnownProjection(" + type + ", " + action + ")";
		}
	}
}
