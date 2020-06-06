package dev.nokee.platform.nativebase.internal.dependencies;

import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.internal.Cast;

import javax.inject.Inject;

public abstract class DefaultDependencyBucket implements DependencyBucket {
	private final Configuration bucket;

	@Inject
	protected abstract DependencyHandler getDependencies();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	public DefaultDependencyBucket(Configuration bucket) {
		this.bucket = bucket;
	}

	@Override
	public void addDependency(Object notation) {
		Dependency dependency = getDependencies().create(notation);
		onNewDependency(dependency);
		bucket.getDependencies().add(dependency);
	}

	@Override
	public <T extends ModuleDependency> void addDependency(Object notation, Action<? super T> action) {
		Dependency dependency = getDependencies().create(notation);
		onNewDependency(dependency);
		action.execute(Cast.uncheckedCast(dependency));
		bucket.getDependencies().add(dependency);
	}

	@Override
	public Configuration getAsConfiguration() {
		return bucket;
	}

	protected void onNewDependency(Dependency dependency) {
		// do nothing
	}
}
