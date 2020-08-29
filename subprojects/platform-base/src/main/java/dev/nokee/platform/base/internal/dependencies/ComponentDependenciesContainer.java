package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.DependencyBucketName;
import org.gradle.api.Action;

import java.util.Optional;

// TODO: Document
public interface ComponentDependenciesContainer {
	<T extends DependencyBucket> T register(DependencyBucketName name, Class<T> type);
	<T extends DependencyBucket> T register(DependencyBucketName name, Class<T> type, Action<? super T> action);
	void configureEach(Action<? super DependencyBucket> action);
	Optional<DependencyBucket> findByName(DependencyBucketName name);
}
