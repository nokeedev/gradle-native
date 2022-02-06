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
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.function.Predicate;

public final class ModelSpecs {
	private ModelSpecs() {}

	/**
	 * Returns a specification that always evaluates to {@code false}.
	 *
	 * @return a {@link ModelSpec} that always evaluates to {@code false}, never null.
	 */
	public static ModelSpec satisfyAll() {
		return ModelNodeSpec.SATISFY_ALL;
	}

	/**
	 * Returns a specification that always evaluates to {@code true}.
	 *
	 * @return a {@link ModelSpec} that always evaluates to {@code true}, never null.
	 */
	public static ModelSpec satisfyNone() {
		return ModelNodeSpec.SATISFY_NONE;
	}

	private enum ModelNodeSpec implements ModelSpec {
		SATISFY_ALL {
			@Override
			public boolean isSatisfiedBy(ModelNode node) {
				return true;
			}

			@Override
			public String toString() {
				return "ModelSpecs.satisfyAll()";
			}
		},
		SATISFY_NONE {
			@Override
			public boolean isSatisfiedBy(ModelNode node) {
				return false;
			}

			@Override
			public String toString() {
				return "ModelSpecs.satisfyNone()";
			}
		}
	}

	/**
	 * Returns a predicate adapted as a model spec.
	 *
	 * @param predicate  the predicate to adapt
	 * @return a {@link ModelSpec} delegating to the specified predicate, never null.
	 */
	public static ModelSpec of(Predicate<? super ModelNode> predicate) {
		return new OfPredicateModelSpec(predicate);
	}

	@EqualsAndHashCode
	private static final class OfPredicateModelSpec implements ModelSpec, HasInputs {
		private final Predicate<? super ModelNode> predicate;
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		public OfPredicateModelSpec(Predicate<? super ModelNode> predicate) {
			this.predicate = predicate;
			if (predicate instanceof HasInputs) {
				this.inputs = ImmutableList.copyOf(((HasInputs) predicate).getInputs());
			} else {
				this.inputs = ImmutableList.of();
			}
			this.inputBits = inputs.stream().map(ModelComponentReference::componentBits).reduce(Bits.empty(), Bits::or);
		}

		@Override
		public boolean isSatisfiedBy(ModelNode node) {
			return predicate.test(node);
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
			return "ModelSpecs.of(" + predicate + ")";
		}
	}
}
