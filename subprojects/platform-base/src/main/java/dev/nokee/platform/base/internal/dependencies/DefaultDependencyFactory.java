package dev.nokee.platform.base.internal.dependencies;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

public final class DefaultDependencyFactory implements DependencyFactory {
	private final DependencyHandler dependencies;

	public DefaultDependencyFactory(DependencyHandler dependencies) {
		this.dependencies = dependencies;
	}

	@Override
	public Dependency create(Object notation) {
		return dependencies.create(notation);
	}
}
