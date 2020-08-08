package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;

public final class DefaultDependencyBucket implements DependencyBucket {
	@Getter private final String name;
	private final Configuration bucket;
	private final DependencyFactory dependencyFactory;

	public DefaultDependencyBucket(String name, Configuration bucket, DependencyFactory dependencyFactory) {
		this.name = name;
		this.bucket = bucket;
		this.dependencyFactory = dependencyFactory;
	}

	@Override
	public void addDependency(Object notation) {
		Dependency dependency = dependencyFactory.create(notation);
		bucket.getDependencies().add(dependency);
	}

	@Override
	public void addDependency(Object notation, Action<? super ModuleDependency> action) {
		Dependency dependency = dependencyFactory.create(notation);
		action.execute((ModuleDependency) dependency);
		bucket.getDependencies().add(dependency);
	}

	@Override
	public Configuration getAsConfiguration() {
		return bucket;
	}
}
