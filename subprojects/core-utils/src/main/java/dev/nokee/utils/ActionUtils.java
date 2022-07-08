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
package dev.nokee.utils;

import com.google.common.collect.Streams;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Transformer;
import org.gradle.api.specs.Spec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static dev.nokee.utils.TransformerUtils.isNoOpTransformer;
import static java.util.Objects.requireNonNull;

public final class ActionUtils {
	/**
	 * Creates an action implementation that simply does nothing.
	 *
	 * @param <T> the type of the executing objects.
	 * @return an action object with an empty implementation, never null.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Action<T> doNothing() {
		return (Action<T>) DoNothingAction.INSTANCE;
	}

	private enum DoNothingAction implements Action<Object>, Serializable {
		INSTANCE;

		@Override
		public void execute(Object o) {
			// do nothing
		}

		@Override
		public String toString() {
			return "ActionUtils.doNothing()";
		}
	}

	public static <A, B> Action<A> compose(org.gradle.api.Action<? super B> g, Transformer<? extends B, ? super A> f) {
		if (doesNothing(g)) {
			return doNothing();
		} else if (isNoOpTransformer(f)) {
			return Cast.uncheckedCast("no op transformer implies the output type is the same", g);
		}
		return new ComposeAction<>(g, f);
	}

	/** @see #compose(org.gradle.api.Action, Transformer) */
	@EqualsAndHashCode
	private static final class ComposeAction<A, B> implements Action<A> {
		private final org.gradle.api.Action<? super B> g;
		private final Transformer<? extends B, ? super A> f;

		public ComposeAction(org.gradle.api.Action<? super B> g, Transformer<? extends B, ? super A> f) {
			this.g = requireNonNull(g);
			this.f = requireNonNull(f);
		}

		@Override
		public void execute(A in) {
			g.execute(f.transform(in));
		}

		@Override
		public String toString() {
			return "ActionUtils.compose(" + g + ", " + f + ")";
		}
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <T> Action<T> composite(org.gradle.api.Action<? super T>... actions) {
		List<org.gradle.api.Action<? super T>> filtered = new ArrayList<>(actions.length);
		for (org.gradle.api.Action<? super T> action : actions) {
			if (doesSomething(action)) {
				filtered.add(action);
			}
		}
		return composite(filtered);
	}

	public static <T> Action<T> composite(Iterable<? extends org.gradle.api.Action<? super T>> actions) {
		val filtered = StreamSupport.stream(actions.spliterator(), false).filter(ActionUtils::doesSomething).collect(Collectors.toList());
		return composite(filtered);
	}

	private static <T> Action<T> composite(List<? extends org.gradle.api.Action<? super T>> actions) {
		switch (actions.size()) {
			case 0: return doNothing();
			case 1: return Action.of(actions.get(0));
			default: return new CompositeAction<T>(actions);
		}
	}

	private static final class CompositeAction<T> implements Action<T> {
		private final Iterable<? extends org.gradle.api.Action<? super T>> actions;

		public CompositeAction(Iterable<? extends org.gradle.api.Action<? super T>> actions) {
			this.actions = actions;
		}

		@Override
		public void execute(T t) {
			for (org.gradle.api.Action<? super T> action : actions) {
				action.execute(t);
			}
		}

		@Override
		public String toString() {
			return "ActionUtils.composite(" + Streams.stream(actions).map(Object::toString).collect(Collectors.joining(", ")) + ")";
		}
	}

	static boolean doesSomething(org.gradle.api.Action<?> action) {
		return action != DoNothingAction.INSTANCE;
	}

	static boolean doesNothing(org.gradle.api.Action<?> action) {
		return action == DoNothingAction.INSTANCE;
	}

	/**
	 * Returns an action delegating to the specified action only if the spec is satisfied by the object passed during execution.
	 *
	 * @param spec a spec to filter the executing objects.
	 * @param action an action to delegate to when the spec is satisfied.
	 * @param <T> the type of the executing objects.
	 * @return an action delegating to the specified action only if the spec is satisfied at execution, never null.
	 */
	public static <T> Action<T> onlyIf(Spec<? super T> spec, org.gradle.api.Action<? super T> action) {
		if (SpecUtils.isSatisfyAll(spec)) {
			return Action.of(action);
		} else if (SpecUtils.isSatisfyNone(spec)) {
			return doNothing();
		}
		return new SpecFilteringAction<>(spec, action);
	}

	@EqualsAndHashCode
	private static final class SpecFilteringAction<T> implements Action<T> {
		private final Spec<? super T> spec;
		private final org.gradle.api.Action<? super T> action;

		private SpecFilteringAction(Spec<? super T> spec, org.gradle.api.Action<? super T> action) {
			this.spec = requireNonNull(spec);
			this.action = requireNonNull(action);
		}

		@Override
		public void execute(T t) {
			if (spec.isSatisfiedBy(t)) {
				action.execute(t);
			}
		}

		@Override
		public String toString() {
			return "ActionUtils.onlyIf(" + spec + ", " + action + ")";
		}
	}

	@FunctionalInterface
	public interface Action<T> extends org.gradle.api.Action<T> {
		static <T> Action<T> of(org.gradle.api.Action<? super T> action) {
			if (action instanceof Action) {
				@SuppressWarnings("unchecked")
				val result = (Action<T>) action;
				return result;
			} else if (doesSomething(action)) {
				return action::execute;
			}
			return doNothing();
		}

		default Action<T> andThen(org.gradle.api.Action<? super T> after) {
			return composite(this, after);
		}
	}
}
