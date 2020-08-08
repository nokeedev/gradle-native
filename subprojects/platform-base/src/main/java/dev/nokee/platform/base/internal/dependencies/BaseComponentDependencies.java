package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.internal.metaobject.MethodAccess;
import org.gradle.internal.metaobject.MethodMixIn;
import org.gradle.internal.metaobject.PropertyAccess;
import org.gradle.internal.metaobject.PropertyMixIn;

import java.util.Optional;

/**
 * Any custom typed component dependencies should extend this type to benefit from the Groovy DSL mixed in.
 */
public class BaseComponentDependencies implements ComponentDependenciesInternal, MethodMixIn, PropertyMixIn {
	private final ComponentDependenciesInternal delegate;

	protected BaseComponentDependencies(ComponentDependenciesInternal delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getComponentDisplayName() {
		return delegate.getComponentDisplayName();
	}

	@Override
	public DependencyBucket create(String name) {
		return delegate.create(name);
	}

	@Override
	public DependencyBucket create(String name, Action<Configuration> action) {
		return delegate.create(name, action);
	}

	@Override
	public DependencyBucket getByName(String name) {
		return delegate.getByName(name);
	}

	@Override
	public void add(String bucketName, Object notation) {
		delegate.add(bucketName, notation);
	}

	@Override
	public void add(String bucketName, Object notation, Action<? super ModuleDependency> action) {
		delegate.add(bucketName, notation, action);
	}

	@Override
	public void configureEach(Action<? super DependencyBucket> action) {
		delegate.configureEach(action);
	}

	@Override
	public Optional<DependencyBucket> findByName(String name) {
		return delegate.findByName(name);
	}

	@Override
	public MethodAccess getAdditionalMethods() {
		return ((DefaultComponentDependencies) delegate).getAdditionalMethods();
	}

	@Override
	public PropertyAccess getAdditionalProperties() {
		return ((DefaultComponentDependencies) delegate).getAdditionalProperties();
	}
}
