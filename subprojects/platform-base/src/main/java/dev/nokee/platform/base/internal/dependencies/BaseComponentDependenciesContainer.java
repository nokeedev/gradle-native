package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DeclarableDependencyBucket;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.DependencyBucketName;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.GroovyObjectSupport;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

import java.util.Optional;

/**
 * Any custom typed component dependencies should extend this type to avoid reimplementing the delegation.
 */
// Not abstract to have compilation error when ComponentDependenciesContainer interface changes.
public /*abstract*/ class BaseComponentDependenciesContainer extends GroovyObjectSupport implements ComponentDependenciesContainer {
	private final ComponentDependenciesContainer delegate;

	public BaseComponentDependenciesContainer(ComponentDependenciesContainer delegate) {
		this.delegate = delegate;
	}

	@Override
	public <T extends DependencyBucket> T register(DependencyBucketName name, Class<T> type) {
		return delegate.register(name, type);
	}

	@Override
	public <T extends DependencyBucket> T register(DependencyBucketName name, Class<T> type, Action<? super T> action) {
		return delegate.register(name, type, action);
	}

	@Override
	public void configureEach(Action<? super DependencyBucket> action) {
		delegate.configureEach(action);
	}

	public void configureEach(@DelegatesTo(DependencyBucket.class) Closure<Void> closure) {
		delegate.configureEach(ConfigureUtil.configureUsing(closure));
	}

	@Override
	public Optional<DependencyBucket> findByName(DependencyBucketName name) {
		return delegate.findByName(name);
	}

	@Override
	public Object invokeMethod(String name, Object args) {
		if (args instanceof Object[]) {
			val arguments = (Object[])args;
			if (arguments.length == 1) {
				val bucket = findByName(DependencyBucketName.of(name));
				if (bucket.isPresent() && bucket.get() instanceof DeclarableDependencyBucket) {
					((DeclarableDependencyBucket) bucket.get()).addDependency(arguments[0]);
					return null;
				}
			} else if (arguments.length == 2 && arguments[1] instanceof Closure) {
				val bucket = findByName(DependencyBucketName.of(name));
				if (bucket.isPresent() && bucket.get() instanceof DeclarableDependencyBucket) {
					((DeclarableDependencyBucket) bucket.get()).addDependency(arguments[0], ConfigureUtil.configureUsing((Closure) arguments[1]));
					return null;
				}
			}
		}
		return super.invokeMethod(name, args);
	}

	@Override
	public Object getProperty(String name) {
		return findByName(DependencyBucketName.of(name)).orElseGet(() -> (DependencyBucket)super.getProperty(name));
	}
}
