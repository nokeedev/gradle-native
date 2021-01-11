package dev.nokee.utils;

import dev.nokee.ChainingAction;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Action;
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

	// TODO: ChainingAction is using it, make private
	public enum DoNothingAction implements ChainingAction<Object>, Action<Object>, Serializable {
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
    public static <T> Action<? super T> composite(Action<? super T>... actions) {
        return composite(Arrays.stream(actions).filter(ActionUtils::doesSomething).collect(Collectors.toList()));
    }

	public static <T> Action<? super T> composite(Iterable<? extends Action<? super T>> actions) {
		List<? extends Action<? super T>> filtered = StreamSupport.stream(actions.spliterator(), false).filter(ActionUtils::doesSomething).collect(Collectors.toList());
		return composite(filtered);
	}

	private static <T> Action<? super T> composite(List<? extends Action<? super T>> actions) {
		switch (actions.size()) {
			case 0: return doNothing();
			case 1: return actions.get(0);
			default: return new CompositeAction<T>(actions);
		}
	}

	private static final class CompositeAction<T> implements Action<T> {
		private final Iterable<? extends Action<? super T>> actions;

		public CompositeAction(Iterable<? extends Action<? super T>> actions) {
			this.actions = actions;
		}

		@Override
		public void execute(T t) {
			for (org.gradle.api.Action<? super T> action : actions) {
				action.execute(t);
			}
		}
	}

	public static boolean doesSomething(Action<?> action) {
		return action != DoNothingAction.INSTANCE;
	}

    public static <T> ChainingAction<? super T> chain(Action<? super T> action) {
        return ChainingAction.of(action);
    }

	/**
	 * Returns an action delegating to the specified action only if the spec is satisfied by the object passed during execution.
	 *
	 * @param spec a spec to filter the executing objects.
	 * @param action an action to delegate to when the spec is satisfied.
	 * @param <T> the type of the executing objects.
	 * @return an action delegating to the specified action only if the spec is satisfied at execution, never null.
	 */
	public static <T> Action<T> onlyIf(Spec<? super T> spec, Action<? super T> action) {
		if (Specs.satisfyAll().equals(spec)) {
			@SuppressWarnings("unchecked")
			val result = (Action<T>) action;
			return result;
		} else if (Specs.satisfyNone().equals(spec)) {
			return doNothing();
		}
		return new SpecFilteringAction<>(spec, action);
	}

	@EqualsAndHashCode
	private static final class SpecFilteringAction<T> implements Action<T> {
		private final Spec<? super T> spec;
		private final Action<? super T> action;

		private SpecFilteringAction(Spec<? super T> spec, Action<? super T> action) {
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

	/**
	 * Returns an action delegating to the specified action only if the object passed during execution is an instance of the specified type.
	 *
	 * @param type the type check to satisfy before delegating.
	 * @param action an action to delegate to if the type matches.
	 * @param <T> the input type
	 * @param <S> the output type
	 * @return an action delegating to the specified action only if the object type matches at execution, never null.
	 */
	public static <T, S> Action<T> onlyIf(Class<S> type, Action<? super S> action) {
		return new TypeFilteringAction<>(type, action);
	}

	@EqualsAndHashCode
	private static final class TypeFilteringAction<T, S> implements Action<T> {
		private final Class<S> type;
		private final Action<? super S> action;

		public TypeFilteringAction(Class<S> type, Action<? super S> action) {
			this.type = requireNonNull(type);
			this.action = requireNonNull(action);
		}

		@Override
		public void execute(T t) {
			if (type.isInstance(t)) {
				action.execute(type.cast(t));
			}
		}

		@Override
		public String toString() {
			return "ActionUtils.onlyIf(" + type.getCanonicalName() + ", " + action + ")";
		}
	}
}
