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

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.function.Predicate;

import static dev.nokee.model.internal.state.ModelState.Realized;
import static dev.nokee.model.internal.state.ModelState.Registered;
import static java.util.Objects.requireNonNull;

/**
 * A group of common model node operations.
 */
public final class ModelNodes {
	private ModelNodes() {}

	/**
	 * Returns a predicate that evaluates to true if both of its components evaluate to true.
	 * The components are evaluated in order, and evaluation will be "short-circuited" as soon as a false predicate is found.
	 *
	 * @param first  the first predicate to evaluate
	 * @param second  the second predicate to evaluate
	 * @return a predicate evaluating to true only if both predicates evaluates to true.
	 */
	public static Predicate<ModelNode> and(Predicate<? super ModelNode> first, Predicate<? super ModelNode> second) {
		return new AndPredicate(first, second);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class AndPredicate extends AbstractModelNodePredicate implements HasInputs {
		private final Predicate<? super ModelNode> first;
		private final Predicate<? super ModelNode> second;
		private final Bits inputBits;
		private final List<ModelComponentReference<?>> inputs;

		public AndPredicate(Predicate<? super ModelNode> first, Predicate<? super ModelNode> second) {
			this.first = requireNonNull(first);
			this.second = requireNonNull(second);
			val builder = ImmutableList.<ModelComponentReference<?>>builder();
			if (first instanceof HasInputs) {
				builder.addAll(((HasInputs) first).getInputs());
			}
			if (second instanceof HasInputs) {
				builder.addAll(((HasInputs) second).getInputs());
			}
			this.inputs = builder.build();
			this.inputBits = inputs.stream().map(ModelComponentReference::componentBits).reduce(Bits.empty(), Bits::or);
		}

		@Override
		public boolean test(ModelNode node) {
			return first.test(node) && second.test(node);
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
			return "ModelNodes.and(" + first + ", " + second + ")";
		}
	}

	// TODO: All custom predicate here should extends from this base predicate
	private static abstract class AbstractModelNodePredicate implements Predicate<ModelNode> {
		// TODO: We should test this method for all custom predicate
		@Override
		public Predicate<ModelNode> and(Predicate<? super ModelNode> other) {
			return new AndPredicate(this, other);
		}

		// TODO: Support or
		// TODO: Support negate
	}

	/**
	 * Returns the decorated model node of the specified instance.
	 * It is safe to say that for managed model node projection instance, a model node will be present.
	 *
	 * @param target  the instance to cross-reference with it's model node
	 * @return the model node for the specified instance if available
	 */
	public static ModelNode of(Object target) {
		requireNonNull(target);
		if (target instanceof ModelNodeAware) {
			return ((ModelNodeAware) target).getNode();
		} else if (target instanceof ExtensionAware) {
			val node = ((ExtensionAware) target).getExtensions().findByType(ModelNode.class);
			if (node == null) {
				throw objectNotDecoratedWithModelNode(target);
			}
			return node;
		} else if (target instanceof ModelNode) {
			return (ModelNode) target;
		}
		throw objectNotDecoratedWithModelNode(target);
	}

	public static <T> T inject(T target, ModelNode node) {
		requireNonNull(target);
		requireNonNull(node);
		assert target instanceof ExtensionAware;
		val extensions = ((ExtensionAware) target).getExtensions();
		val currentNode = extensions.findByName("__NOKEE_modelNode");
		if (currentNode == null) {
			((ExtensionAware) target).getExtensions().add(ModelNode.class, "__NOKEE_modelNode", node);
		} else if (currentNode != node) {
			throw new IllegalStateException("Injecting a different model node!");
		}
		return target;
	}

	/**
	 * Returns a predicate filtering model nodes by the specified type.
	 *
	 * @param type  the type to filter model node
	 * @return a predicate matching model nodes by type, never null.
	 */
	// TODO: Rename to subtypeOf
	public static Predicate<ModelNode> withType(ModelType<?> type) {
		return new WithTypePredicate(type);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class WithTypePredicate extends AbstractModelNodePredicate implements HasInputs {
		private final ModelType<?> type;
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		private WithTypePredicate(ModelType<?> type) {
			this.type = requireNonNull(type);
			this.inputs = ImmutableList.of(ModelComponentReference.ofProjection(type.getConcreteType()));
			this.inputBits = inputs.stream().map(ModelComponentReference::componentBits).reduce(Bits.empty(), Bits::or);
		}

		@Override
		public boolean test(ModelNode node) {
			return ModelNodeUtils.canBeViewedAs(node, type);
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
			return "ModelNodes.withType(" + type + ")";
		}
	}

	/**
	 * Returns a predicate filtering model nodes by the specified state.
	 *
	 * @param state  the state to filter model node
	 * @return a predicate matching model node by state, never null.
	 */
	public static Predicate<ModelNode> stateAtLeast(ModelState state) {
		return new StateAtLeastPredicate(state);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class StateAtLeastPredicate extends AbstractModelNodePredicate implements HasInputs {
		private final ModelState state;
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		private StateAtLeastPredicate(ModelState state) {
			this.state = requireNonNull(state);
			this.inputs = ImmutableList.of(ModelComponentReference.of(ModelState.class));
			this.inputBits = inputs.stream().map(ModelComponentReference::componentBits).reduce(Bits.empty(), Bits::or);
		}

		@Override
		public boolean test(ModelNode node) {
			return ModelStates.isAtLeast(node, state);
		}

		@Override
		public String toString() {
			return "ModelNodes.stateAtLeast(" + state + ")";
		}

		@Override
		public List<? extends ModelComponentReference<?>> getInputs() {
			return inputs;
		}

		@Override
		public Bits getInputBits() {
			return inputBits;
		}
	}

	public static Predicate<ModelNode> stateOf(ModelState state) {
		return new StateOfPredicate(state);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class StateOfPredicate extends AbstractModelNodePredicate implements HasInputs {
		private final ModelState state;
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		private StateOfPredicate(ModelState state) {
			this.state = requireNonNull(state);
			this.inputs = ImmutableList.of(ModelComponentReference.of(ModelState.class));
			this.inputBits = inputs.stream().map(ModelComponentReference::componentBits).reduce(Bits.empty(), Bits::or);
		}

		@Override
		public boolean test(ModelNode node) {
			return ModelStates.getState(node).equals(state);
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
			return "ModelNodes.stateOf(" + state + ")";
		}
	}

	/**
	 * Returns a predicate filtering model nodes that satisfy the specified spec for the projection type specified.
	 *
	 * @param type  the projection type to match using the spec
	 * @param spec  the spec to satisfy using a projection of the model node
	 * @param <T> the projection type
	 * @return a predicate matching model nodes by spec of a projection, never null.
	 */
	public static <T> Predicate<ModelNode> isSatisfiedByProjection(ModelType<T> type, Spec<? super T> spec) {
		return new SatisfiedByProjectionSpecAdapter<>(type, spec);
	}

	private static final class SatisfiedByProjectionSpecAdapter<T> extends AbstractModelNodePredicate implements HasInputs {
		private final ModelType<T> type;
		private final Spec<? super T> spec;
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		private SatisfiedByProjectionSpecAdapter(ModelType<T> type, Spec<? super T> spec) {
			this.type = requireNonNull(type);
			this.spec = requireNonNull(spec);
			val builder = ImmutableList.<ModelComponentReference<?>>builder();
			builder.add(ModelComponentReference.ofProjection(type.getConcreteType()));
			if (spec instanceof HasInputs) {
				builder.addAll(((HasInputs) spec).getInputs());
			}
			this.inputs = builder.build();
			this.inputBits = inputs.stream().map(ModelComponentReference::componentBits).reduce(Bits.empty(), Bits::or);
		}

		@Override
		public boolean test(ModelNode node) {
			return ModelNodeUtils.canBeViewedAs(node, type) && spec.isSatisfiedBy(ModelNodeUtils.get(node, type));
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
			return "ModelNodes.isSatisfiedByProjection(" + type + ", " + spec + ")";
		}
	}

	/**
	 * Returns a predicate filtering model nodes by the specified parent path.
	 *
	 * @param parentPath  the parent path to match model nodes
	 * @return a predicate matching model nodes by parent path, never null.
	 */
	// TODO: Rename to directDescendantOf
	public static Predicate<ModelNode> withParent(ModelPath parentPath) {
		return new WithParentPredicate(parentPath);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class WithParentPredicate extends AbstractModelNodePredicate implements HasInputs {
		private final ModelPath parentPath;
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		public WithParentPredicate(ModelPath parentPath) {
			this.parentPath = requireNonNull(parentPath);
			this.inputs = ImmutableList.of(ModelComponentReference.of(ModelPathComponent.class));
			this.inputBits = inputs.stream().map(ModelComponentReference::componentBits).reduce(Bits.empty(), Bits::or);
		}

		@Override
		public boolean test(ModelNode node) {
			return node.find(ModelPathComponent.class)
				.map(ModelPathComponent::get)
				.flatMap(ModelPath::getParent)
				.map(parentPath::equals)
				.orElse(false);
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
			return "ModelNodes.withParent(" + parentPath + ")";
		}
	}

	public static Predicate<ModelNode> descendantOf(ModelPath ancestorPath) {
		return new DescendantOfPredicate(ancestorPath);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class DescendantOfPredicate extends AbstractModelNodePredicate implements HasInputs {
		private final ModelPath ancestorPath;
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		public DescendantOfPredicate(ModelPath ancestorPath) {
			this.ancestorPath = requireNonNull(ancestorPath);
			this.inputs = ImmutableList.of(ModelComponentReference.of(ModelPathComponent.class));
			this.inputBits = inputs.stream().map(ModelComponentReference::componentBits).reduce(Bits.empty(), Bits::or);
		}

		@Override
		public boolean test(ModelNode node) {
			return node.find(ModelPathComponent.class)
				.map(ModelPathComponent::get)
				.map(ancestorPath::isDescendant)
				.orElse(false);
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
			return "ModelNodes.descendantOf(" + ancestorPath + ")";
		}
	}

	public static Predicate<ModelNode> discover() {
		return stateOf(Registered);
	}

	public static Predicate<ModelNode> discover(ModelType<?> type) {
		return stateOf(Registered).and(withType(type));
	}

	public static Predicate<ModelNode> mutate(ModelType<?> type) {
		return stateOf(Realized).and(withType(type));
	}

	/**
	 * Returns a predicate that select the specified path in the model.
	 *
	 * @param path  the path to match a model node
	 * @return a predicate matching a single model node of the specified path, never null.
	 */
	// TODO: Maybe rename to pathOf(
	public static Predicate<ModelNode> withPath(ModelPath path) {
		return new WithPathPredicate(path);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class WithPathPredicate extends AbstractModelNodePredicate implements HasInputs {
		private final ModelPath path;
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		public WithPathPredicate(ModelPath path) {
			this.path = requireNonNull(path);
			this.inputs = ImmutableList.of(ModelComponentReference.of(ModelPathComponent.class));
			this.inputBits = inputs.stream().map(ModelComponentReference::componentBits).reduce(Bits.empty(), Bits::or);
		}

		@Override
		public boolean test(ModelNode node) {
			return node.find(ModelPathComponent.class).map(ModelPathComponent::get).map(path::equals).orElse(false);
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
			return "ModelNodes.withPath(" + path + ")";
		}
	}

	private static IllegalArgumentException objectNotDecoratedWithModelNode(Object target) {
		return new IllegalArgumentException(String.format("Object '%s' is not decorated with ModelNode.", target));
	}
}
