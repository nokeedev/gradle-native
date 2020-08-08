package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

import java.util.Optional;

public interface ComponentDependenciesInternal {
	String getComponentDisplayName(); // This should be an model identity that includes the component information
	DependencyBucket create(String name) throws InvalidUserDataException;
	DependencyBucket create(String name, Action<Configuration> action) throws InvalidUserDataException;
	DependencyBucket getByName(String name) throws UnknownDomainObjectException;
	void add(String bucketName, Object notation);
	void add(String bucketName, Object notation, Action<? super ModuleDependency> action);
	void configureEach(Action<? super DependencyBucket> action);
	Optional<DependencyBucket> findByName(String name);
}
