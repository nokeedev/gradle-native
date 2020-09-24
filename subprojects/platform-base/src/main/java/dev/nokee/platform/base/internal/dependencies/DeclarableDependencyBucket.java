package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;

/**
 * Represent a bucket of declarable dependencies.
 * These dependencies are neither incoming or outgoing dependencies.
 *
 * @since 0.5
 */
public interface DeclarableDependencyBucket extends DependencyBucket {
	void addDependency(Object notation);

	void addDependency(Object notation, Action<? super ModuleDependency> action);
}
