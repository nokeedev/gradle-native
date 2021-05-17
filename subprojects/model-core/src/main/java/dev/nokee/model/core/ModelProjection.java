package dev.nokee.model.core;

public interface ModelProjection {

	// TODO: Not sure
	<T> boolean canBeViewedAs(Class<T> type);

	<T> T get(Class<T> type);
}
