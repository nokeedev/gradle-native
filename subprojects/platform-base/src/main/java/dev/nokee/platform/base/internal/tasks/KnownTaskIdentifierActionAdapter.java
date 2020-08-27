package dev.nokee.platform.base.internal.tasks;

import org.gradle.api.Action;
import org.gradle.api.Task;

final class KnownTaskIdentifierActionAdapter implements Action<Task> {
	private final KnownTaskIdentifiers knownTaskIdentifiers;
	private final Action<? super Task> delegate;

	public KnownTaskIdentifierActionAdapter(KnownTaskIdentifiers knownTaskIdentifiers, Action<? super Task> delegate) {
		this.knownTaskIdentifiers = knownTaskIdentifiers;
		this.delegate = delegate;
	}

	@Override
	public void execute(Task task) {
		if (knownTaskIdentifiers.contains(task.getName())) {
			delegate.execute(task);
		}
	}
}
