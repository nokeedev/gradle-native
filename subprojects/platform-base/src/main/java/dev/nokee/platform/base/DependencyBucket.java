package dev.nokee.platform.base;

import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

public interface DependencyBucket {
	String getName();

	void addDependency(Object notation);

	void addDependency(Object notation, Action<? super ModuleDependency> action);

	Configuration getAsConfiguration();
}
