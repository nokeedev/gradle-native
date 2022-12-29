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
package dev.nokee.xcode.project.coders;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

abstract class Select<T, R> {

	public static final class SwitchCase<T, C, R> extends Select<T, R> {
		private final Map<C, Function<? super T, ? extends R>> cases = new LinkedHashMap<>();
		private final Function<? super T, ? extends C> switchTransformer;

		public SwitchCase(Function<? super T, ? extends C> switchTransformer) {
			this.switchTransformer = switchTransformer;
		}

		public SwitchCase<T, C, R> forCase(C instance, Function<? super T, ? extends R> execution) {
			cases.put(instance, execution);
			return this;
		}

		public <RR extends R> RR select(T instance) {
			final C inputValue = switchTransformer.apply(instance);
			final Function<? super T, ? extends R> execution = cases.get(inputValue);

			if (execution == null) {
				throw new IllegalArgumentException();
			}

			@SuppressWarnings("unchecked")
			RR result = (RR) execution.apply(instance);
			return result;
		}
	}

	public static <T, C, R> SwitchCase<T, C, R> newInstance(Function<? super T, ? extends C> switchTransformer) {
		return new SwitchCase<>(switchTransformer);
	}

	public static <T, R> LookupTable<T, R> newInstance() {
		return new LookupTable<>();
	}

	public static final class LookupTable<T, R> extends Select<T, R> {
		private final Map<T, R> cases = new LinkedHashMap<>();

		public LookupTable<T, R> forCase(T instance, R result) {
			cases.put(instance, result);
			return this;
		}

		@Override
		public <RR extends R> RR select(T instance) {
			final R execution = cases.get(instance);

			if (execution == null) {
				throw new IllegalArgumentException();
			}

			@SuppressWarnings("unchecked")
			RR result = (RR) execution;
			return result;
		}
	}

	public static <T, R> IfElseCases<T, R> newIfElseCases() {
		return new IfElseCases<>();
	}

	public static final class IfElseCases<T, R> extends Select<T, R> {
		private final List<IfElseCase> cases = new ArrayList<>();

		public IfElseCases<T, R> forCase(Predicate<? super T> predicate, Function<? super T, ? extends R> execution) {
			cases.add(new IfElseCase(predicate, execution));
			return this;
		}

		@Override
		public <RR extends R> RR select(T instance) {
			for (IfElseCase aCase : cases) {
				if (aCase.predicate.test(instance)) {
					@SuppressWarnings("unchecked")
					RR result = (RR) aCase.execution.apply(instance);
					return result;
				}
			}

			throw new IllegalArgumentException();
		}

		private final class IfElseCase {
			private final Predicate<? super T> predicate;
			private final Function<? super T, ? extends R> execution;

			public IfElseCase(Predicate<? super T> predicate, Function<? super T, ? extends R> execution) {
				this.predicate = predicate;
				this.execution = execution;
			}
		}
	}

	public abstract <RR extends R> RR select(T instance);
}
