package dev.nokee.platform.base.internal.dependencies;

import org.gradle.api.artifacts.Dependency;

public interface DependencyFactory {
	Dependency create(Object notation);
}
