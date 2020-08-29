package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.ConsumableDependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ConfigurationPublications;
import org.gradle.api.attributes.AttributeContainer;

/**
 * Base class for all custom consumable dependency buckets.
 * It purposefully don't expose the Configuration object.
 */
// Not abstract to have compilation error when ConsumableDependencyBucket interface changes.
public /*abstract*/ class BaseConsumableDependencyBucket extends AbstractDependencyBucket implements ConsumableDependencyBucket {
	protected BaseConsumableDependencyBucket() {
		this(DependencyBucketFactoryFactory.getNextDependencyBucketInfo());
	}

	private BaseConsumableDependencyBucket(DependencyBucketFactoryFactory.DependencyBucketInfo info) {
		super(info.getIdentifier(), info.getConfiguration());
	}

	protected AttributeContainer getAttributes() {
		return configuration.getAttributes();
	}

	protected void attributes(Action<? super AttributeContainer> action) {
		configuration.attributes(action);
	}

	protected ConfigurationPublications getOutgoing() {
		return configuration.getOutgoing();
	}
}
