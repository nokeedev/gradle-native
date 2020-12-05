package dev.nokee.model.internal.core;

import dev.nokee.model.internal.type.ModelType;

public interface ModelProjection {

	// TODO: Not sure here
	<T> boolean canBeViewedAs(ModelType<T> type);

	<T> T get(ModelType<T> type);
}
