/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.VariantDimensionBuilder;
import dev.nokee.runtime.core.Coordinates;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

final class VariantDimensionBuilderAdapter<T> implements VariantDimensionBuilder<T> {
	private final Callback<T> delegate;

	public VariantDimensionBuilderAdapter(Callback<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public VariantDimensionBuilder<T> onlyOn(Object otherAxisValue) {
		Objects.requireNonNull(otherAxisValue);
		delegate.accept(Coordinates.of(otherAxisValue).getAxis().getType(), new OnlyOnPredicate<>(otherAxisValue));
		return this;
	}

	@Override
	public VariantDimensionBuilder<T> exceptOn(Object otherAxisValue) {
		Objects.requireNonNull(otherAxisValue);
		delegate.accept(Coordinates.of(otherAxisValue).getAxis().getType(), new OnlyOnPredicate<T, Object>(otherAxisValue).negate());
		return this;
	}

	@Override
	public <S> VariantDimensionBuilder<T> onlyIf(Class<S> otherAxisType, BiPredicate<? super Optional<T>, ? super S> predicate) {
		Objects.requireNonNull(otherAxisType);
		Objects.requireNonNull(predicate);
		delegate.accept(otherAxisType, predicate);
		return this;
	}

	@Override
	public <S> VariantDimensionBuilder<T> exceptIf(Class<S> otherAxisType, BiPredicate<? super Optional<T>, ? super S> predicate) {
		Objects.requireNonNull(otherAxisType);
		Objects.requireNonNull(predicate);
		delegate.accept(otherAxisType, predicate.negate());
		return this;
	}

	public interface Callback<T> {
		<S> void accept(Class<S> otherAxisType, BiPredicate<? super Optional<T>, ? super S> predicate);
	}

	public static final class OnlyOnPredicate<T, S> implements BiPredicate<Optional<T>, S> {
		private final Object otherAxisValue;

		private OnlyOnPredicate(Object otherAxisValue) {
			this.otherAxisValue = otherAxisValue;
		}

		@Override
		public boolean test(Optional<T> currentValue, S otherValue) {
			return !currentValue.isPresent() || otherValue.equals(otherAxisValue);
		}
	}
}
