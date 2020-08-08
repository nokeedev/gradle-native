package dev.nokee.utils.internal;

import org.gradle.api.Action;

public class CompositeAction<T> implements Action<T> {
	private final Iterable<? extends Action<? super T>> actions;

	public CompositeAction(Iterable<? extends Action<? super T>> actions) {
		this.actions = actions;
	}

	@Override
	public void execute(T t) {
		for (Action<? super T> action : actions) {
			action.execute(t);
		}
	}
}
