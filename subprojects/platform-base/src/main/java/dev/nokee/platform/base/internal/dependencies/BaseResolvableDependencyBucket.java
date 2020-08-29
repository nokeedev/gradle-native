package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.ResolvableDependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.attributes.AttributeContainer;

// FIXME: UNITE TEST THIS CLASS
/**
 * Base class for all custom resolvable dependency buckets.
 * It purposefully don't expose the Configuration object.
 */
// Not abstract to have compilation error when ResolvableDependencyBucket interface changes.
public /*abstract*/ class BaseResolvableDependencyBucket extends AbstractDependencyBucket implements ResolvableDependencyBucket {
	protected BaseResolvableDependencyBucket() {
		this(DependencyBucketFactoryFactory.getNextDependencyBucketInfo());
	}

	private BaseResolvableDependencyBucket(DependencyBucketFactoryFactory.DependencyBucketInfo info) {
		super(info.getIdentifier(), info.getConfiguration());
	}

	protected AttributeContainer getAttributes() {
		return configuration.getAttributes();
	}

	protected void attributes(Action<? super AttributeContainer> action) {
		configuration.attributes(action);
	}

	protected org.gradle.api.artifacts.ResolvableDependencies getIncoming() {
		return configuration.getIncoming();
	}
}
