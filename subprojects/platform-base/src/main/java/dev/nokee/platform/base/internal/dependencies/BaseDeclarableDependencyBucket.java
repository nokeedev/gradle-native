package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DeclarableDependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

// TODO: Test creating bucket that will reuse a configuration if already existing
/**
 * Base class for all custom declarable dependency buckets.
 * It purposefully don't expose the Configuration object.
 */
// Not abstract to have compilation error when DeclarableDependencyBucket interface changes.
public /*abstract*/ class BaseDeclarableDependencyBucket extends AbstractDependencyBucket implements DeclarableDependencyBucket {
	private final DependencyHandler dependencyFactory;

	protected BaseDeclarableDependencyBucket() {
		this(DependencyBucketFactoryFactory.getNextDependencyBucketInfo());
	}

	private BaseDeclarableDependencyBucket(DependencyBucketFactoryFactory.DependencyBucketInfo info) {
		super(info.getIdentifier(), info.getConfiguration());
		this.dependencyFactory = info.getDependencies();
	}

	@Override
	public void addDependency(Object notation) {
		Dependency dependency = dependencyFactory.create(notation);
		configuration.getDependencies().add(dependency);
	}

	@Override
	public void addDependency(Object notation, Action<? super ModuleDependency> action) {
		Dependency dependency = dependencyFactory.create(notation);
		action.execute((ModuleDependency) dependency);
		configuration.getDependencies().add(dependency);
	}
}
