package dev.nokee.utils;

import dev.nokee.ChainingAction;
import dev.nokee.utils.internal.CompositeAction;
import org.gradle.api.Action;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static dev.nokee.utils.internal.NullAction.DO_NOTHING;

public final class ActionUtils {
	@SuppressWarnings("unchecked")
	public static <T> Action<T> doNothing() {
		return (Action<T>) DO_NOTHING;
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

	public static boolean doesSomething(Action<?> action) {
		return action != DO_NOTHING;
	}

    public static <T> ChainingAction<? super T> chain(Action<? super T> action) {
        return ChainingAction.of(action);
    }
}
