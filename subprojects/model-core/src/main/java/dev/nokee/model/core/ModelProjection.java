package dev.nokee.model.core;

import org.gradle.api.Action;
import org.gradle.api.provider.Provider;

public interface ModelProjection {

	// TODO: Not sure
	<T> boolean canBeViewedAs(Class<T> type);

	<T> T get(Class<T> type);

	<T> Provider<T> as(Class<T> type);

	<T> void whenRealized(Class<T> type, Action<? super T> action);
}
