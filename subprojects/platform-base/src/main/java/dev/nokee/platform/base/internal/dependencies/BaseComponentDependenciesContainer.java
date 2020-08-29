package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DeclarableDependencyBucket;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.DependencyBucketName;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.internal.metaobject.*;
import org.gradle.util.ConfigureUtil;

import java.util.Optional;

/**
 * Any custom typed component dependencies should extend this type to avoid reimplementing the delegation.
 */
// Not abstract to have compilation error when ComponentDependenciesContainer interface changes.
public /*abstract*/ class BaseComponentDependenciesContainer extends GroovyObjectSupport implements ComponentDependenciesContainer, MethodMixIn, PropertyMixIn {
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
		val bucket = findByName(DependencyBucketName.of(name));
		if (!bucket.isPresent()) {
			throw new IllegalArgumentException(String.format("Dependency bucket named '%s' couldn't be found.", name));
		} else if (!(bucket.get() instanceof DeclarableDependencyBucket)) {
			throw new IllegalArgumentException(String.format("Dependency bucket named '%s' isn't declarable.", name));
		}
		return super.invokeMethod(name, args);
	}

	private final ContainerElementsDynamicObject elementsDynamicObject = new ContainerElementsDynamicObject();

	private class ContainerElementsDynamicObject extends AbstractDynamicObject {
		@Override
		public String getDisplayName() {
			return ""; // TODO: Use identifier of the container
		}

		@Override
		public boolean hasProperty(String name) {
			return findByName(DependencyBucketName.of(name)).isPresent();
		}

		@Override
		public Object getProperty(String name) throws MissingPropertyException {
			return findByName(DependencyBucketName.of(name)).orElseThrow(() -> new MissingPropertyException(name, BaseComponentDependenciesContainer.this.getClass()));
		}

		@Override
		public boolean hasMethod(String name, Object... arguments) {
			return isConfigureMethod(name, arguments);
		}

		@Override
		public DynamicInvokeResult tryInvokeMethod(String name, Object... arguments) {
			if (isConfigureMethod(name, arguments)) {
				if (arguments.length == 1) {
					return DynamicInvokeResult.found(register(DependencyBucketName.of(name), (Class)arguments[0]));
				} else if (arguments.length == 2) {
					return DynamicInvokeResult.found(register(DependencyBucketName.of(name), (Class)arguments[0], ConfigureUtil.configureUsing((Closure) arguments[1])));
				}
			}
			return DynamicInvokeResult.notFound();
		}

		private boolean isConfigureMethod(String name, Object... arguments) {
			return (arguments.length == 1 && arguments[0] instanceof Class
				|| arguments.length == 2 && arguments[0] instanceof Class && arguments[1] instanceof Closure);
		}
	}

	protected DynamicObject getElementsAsDynamicObject() {
		return elementsDynamicObject;
	}

	@Override
	public MethodAccess getAdditionalMethods() {
		return getElementsAsDynamicObject();
	}

	@Override
	public PropertyAccess getAdditionalProperties() {
		return getElementsAsDynamicObject();
	}
}
