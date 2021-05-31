package dev.nokee.model.core;

import org.gradle.api.Named;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface ModelNode extends Named {
	ModelNode newChildNode(Object identity);

	ModelProjection newProjection(Consumer<? super ModelProjection.Builder> builderAction);

	Optional<ModelNode> getParent();

	boolean canBeViewedAs(Class<?> type);
	<T> T get(Class<T> type);

	ModelNode get(Object identity);
	Optional<ModelNode> find(Object identity);
	Object getIdentity();

	Stream<ModelNode> getChildNodes();
	Stream<ModelProjection> getProjections();
}
