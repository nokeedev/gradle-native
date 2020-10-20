package dev.nokee.model.internal;

import org.gradle.api.Action;

public interface HasConfigureElementByNameSupport<T> {
	void configure(String name, Action<? super T> action);
	<S extends T> void configure(String name, Class<S> type, Action<? super S> action);
}
