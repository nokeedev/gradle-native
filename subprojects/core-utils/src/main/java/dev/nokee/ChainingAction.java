package dev.nokee;

import dev.nokee.utils.ActionUtils;
import org.gradle.api.Action;

import java.util.Objects;

import static dev.nokee.utils.ActionUtils.doesSomething;

public interface ChainingAction<T> extends Action<T> {
	default ChainingAction<T> andThen(Action<? super T> after) {
		Objects.requireNonNull(after);
		if (doesSomething(after)) {
			return (T t) -> { execute(t); after.execute(t); };
		}
		return this;
	}

	static <T> ChainingAction<T> of(Action<T> action) {
		if (doesSomething(action)) {
			return action::execute;
		}
		return doNothing();
	}

	@SuppressWarnings("unchecked")
	static <T> ChainingAction<T> doNothing() {
		return (ChainingAction<T>) ActionUtils.DoNothingAction.INSTANCE;
	}
}
