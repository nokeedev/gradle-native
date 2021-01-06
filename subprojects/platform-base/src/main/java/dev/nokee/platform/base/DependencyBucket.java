package dev.nokee.platform.base;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketProjection;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

public interface DependencyBucket {
	default String getName() {
		return ModelNodes.of(this).get(DependencyBucketProjection.class).getName();
	}

	default void addDependency(Object notation) {
		ModelNodes.of(this).get(DependencyBucketProjection.class).addDependency(notation);
	}

	default void addDependency(Object notation, Action<? super ModuleDependency> action) {
		ModelNodes.of(this).get(DependencyBucketProjection.class).addDependency(notation, action);
	}

	default Configuration getAsConfiguration() {
		return ModelNodes.of(this).get(Configuration.class);
	}
}
