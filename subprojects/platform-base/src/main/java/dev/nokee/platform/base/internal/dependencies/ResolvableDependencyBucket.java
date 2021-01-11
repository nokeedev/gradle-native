package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.DependencyBucket;
import org.gradle.api.file.FileCollection;

/**
 * Represent a bucket of resolvable dependencies also known as incoming dependencies.
 *
 * @since 0.5
 */
public interface ResolvableDependencyBucket extends DependencyBucket {
	default FileCollection getAsLenientFileCollection() {
		return ModelNodes.of(this).get(ResolvableDependencyBucketRegistrationFactory.IncomingArtifacts.class).getAsLenient();
	}

	default FileCollection getAsFileCollection() {
		return ModelNodes.of(this).get(ResolvableDependencyBucketRegistrationFactory.IncomingArtifacts.class).get();
	}
}
