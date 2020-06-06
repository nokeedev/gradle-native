package dev.nokee.platform.nativebase.internal.dependencies;

import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

public interface DependencyBucket {
	void addDependency(Object notation);

	<T extends ModuleDependency> void addDependency(Object notation, Action<? super T> action);

	Configuration getAsConfiguration();
}
