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
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.google.common.base.Predicates.alwaysTrue;
import static dev.nokee.model.internal.core.ModelNodes.descendantOf;
import static dev.nokee.model.internal.core.ModelNodes.withParent;
import static dev.nokee.model.internal.core.ModelNodes.withPath;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public abstract class NodePredicate {
	private final Predicate<? super ModelNode> matcher;
	private final NodePredicateScopeStrategy scopeStrategy;

	private NodePredicate(Predicate<? super ModelNode> matcher, NodePredicateScopeStrategy scopeStrategy) {
		this.matcher = requireNonNull(matcher);
		this.scopeStrategy = scopeStrategy;
	}

	/**
	 * Returns a model specification based on this predicate and the scoping path.
	 *
	 * @param path  the model path to scope the predicate
	 * @return a {@link ModelSpec} for matching model nodes, never null
	 */
	final ModelSpec scope(ModelPath path) {
		return scopeStrategy.scope(path, matcher);
	}

	abstract void doNotExtendBeyondThisPackage();

	/**
	 * Creates a predicate that matches all direct descendants of the scoped path.
	 *
	 * @return a {@link NodePredicate} matching all direct descendants, never null
	 */
	public static NodePredicate allDirectDescendants() {
		return new NodePredicate(alwaysTrue(), NodePredicateScopeStrategy.ALL_DIRECT_DESCENDANT) {
			@Override
			void doNotExtendBeyondThisPackage() {}

			@Override
			public String toString() {
				return "NodePredicate.allDirectDescendants()";
			}
		};
	}

	/**
	 * Creates a predicate that matches all direct descendants of the scoped path with the specified predicate.
	 *
	 * @param predicate  a predicate to match against all direct descendants
	 * @return a {@link NodePredicate} matching all direct descendants with a predicate, never null
	 */
	public static NodePredicate allDirectDescendants(Predicate<? super ModelNode> predicate) {
		return new NodePredicate(predicate, NodePredicateScopeStrategy.ALL_DIRECT_DESCENDANT) {
			@Override
			void doNotExtendBeyondThisPackage() {}

			@Override
			public String toString() {
				return "NodePredicate.allDirectDescendants(" + predicate + ")";
			}
		};
	}

	public static NodePredicate allDescendants(Predicate<? super ModelNode> predicate) {
		return new NodePredicate(predicate, NodePredicateScopeStrategy.ALL_DESCENDANT) {
			@Override
			void doNotExtendBeyondThisPackage() {}

			@Override
			public String toString() {
				return "NodePredicate.allDescendants(" + predicate + ")";
			}
		};
	}

	public static NodePredicate self() {
		return new NodePredicate(alwaysTrue(), NodePredicateScopeStrategy.SELF) {
			@Override
			void doNotExtendBeyondThisPackage() {}

			@Override
			public String toString() {
				return "NodePredicate.self()";
			}
		};
	}

	@SuppressWarnings("overloads")
	public static NodePredicate self(Predicate<? super ModelNode> predicate) {
		return new NodePredicate(predicate, NodePredicateScopeStrategy.SELF) {
			@Override
			void doNotExtendBeyondThisPackage() {}

			@Override
			public String toString() {
				return "NodePredicate.self(" + predicate + ")";
			}
		};
	}

	private enum NodePredicateScopeStrategy {
		ALL_DIRECT_DESCENDANT {
			@Override
			ModelSpec scope(ModelPath path, Predicate<? super ModelNode> matcher) {
				return new BasicPredicateSpec(null, path, null, withParent(path).and(matcher));
			}
		},
		ALL_DESCENDANT {
			@Override
			ModelSpec scope(ModelPath path, Predicate<? super ModelNode> matcher) {
				return new BasicPredicateSpec(null, path, null, descendantOf(path).and(matcher));
			}
		},
		SELF {
			@Override
			ModelSpec scope(ModelPath path, Predicate<? super ModelNode> matcher) {
				return new BasicPredicateSpec(path, null, null, withPath(path).and(matcher));
			}
		};

		abstract ModelSpec scope(ModelPath path, Predicate<? super ModelNode> matcher);
	}

	@ToString
	@EqualsAndHashCode
	private static final class BasicPredicateSpec implements ModelSpec, HasInputs {
		@Nullable
		private final ModelPath path;

		@Nullable
		private final ModelPath parent;

		@Nullable
		private final ModelPath ancestor;
		private final Predicate<? super ModelNode> matcher;
		@EqualsAndHashCode.Exclude private final Predicate<? super ModelNode> predicate;
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		public BasicPredicateSpec(@Nullable ModelPath path, @Nullable ModelPath parent, @Nullable ModelPath ancestor, Predicate<? super ModelNode> matcher) {
			this.path = path;
			this.parent = parent;
			this.ancestor = ancestor;
			this.matcher = matcher;
			this.predicate = matcher;
			if (matcher instanceof HasInputs) {
				this.inputs = ImmutableList.copyOf(((HasInputs) matcher).getInputs());
			} else {
				this.inputs = ImmutableList.of();
			}
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
		public boolean isSatisfiedBy(ModelNode node) {
			return predicate.test(node);
		}

		@Override
		public Optional<ModelPath> getPath() {
			return Optional.ofNullable(path);
		}

		@Override
		public Optional<ModelPath> getParent() {
			return Optional.ofNullable(parent);
		}

		@Override
		public Optional<ModelPath> getAncestor() {
			return Optional.ofNullable(ancestor);
		}

		@Override
		public Predicate<? super ModelNode> getMatcher() {
			return matcher;
		}
	}
}
