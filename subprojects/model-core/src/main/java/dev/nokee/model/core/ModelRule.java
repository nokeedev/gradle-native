package dev.nokee.model.core;

import dev.nokee.model.streams.ModelStream;

public interface ModelRule {
	void execute(ModelStream<ModelProjection> stream);
}
