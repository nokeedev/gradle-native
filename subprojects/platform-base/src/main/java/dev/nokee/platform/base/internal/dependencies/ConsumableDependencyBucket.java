package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.DependencyBucket;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.provider.Provider;

/**
 * Represent a bucket of consumable dependencies also known as outgoing dependencies.
 *
 * @since 0.5
 */
public interface ConsumableDependencyBucket extends DependencyBucket {
	default ConsumableDependencyBucket artifact(Object artifact) {
		ModelNodes.of(this).get(ConsumableDependencyBucketRegistrationFactory.OutgoingArtifacts.class).getArtifacts().add(new LazyPublishArtifact((Provider<?>) artifact));
		return this;
	}
}
