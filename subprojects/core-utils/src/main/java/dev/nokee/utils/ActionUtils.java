package dev.nokee.utils;

import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

public final class ActionUtils {
	/**
	 * Creates an action implementation that simply does nothing.
	 *
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

    @SafeVarargs
    public static <T> Action<T> composite(org.gradle.api.Action<? super T>... actions) {
        return composite(Arrays.stream(actions).filter(ActionUtils::doesSomething).collect(Collectors.toList()));
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
	}

	public static boolean doesSomething(org.gradle.api.Action<?> action) {
		return action != DoNothingAction.INSTANCE;
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
		if (Specs.satisfyAll().equals(spec) || SpecUtils.satisfyAll().equals(spec)) {
			return Action.of(action);
		} else if (Specs.satisfyNone().equals(spec) || SpecUtils.satisfyNone().equals(spec)) {
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

		default Action<T> andThen(Action<? super T> after) {
			return composite(this, after);
		}
	}
}
