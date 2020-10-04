package dev.nokee.model.internal;

import org.gradle.api.Transformer;

public final class DisallowChangesTransformer<T> implements Transformer<T, T> {
	private boolean changesDisallowed = false;

	@Override
	public T transform(T t) {
		if (changesDisallowed) {
			return t;
		}
		throw new IllegalStateException("Please disallow changes before realizing this collection.");
	}

	public void disallowChanges() {
		changesDisallowed = true;
	}

	public void assertChangesAllowed() {
		if (changesDisallowed) {
			throw new IllegalStateException("The value cannot be changed any further.");
		}
	}
}
