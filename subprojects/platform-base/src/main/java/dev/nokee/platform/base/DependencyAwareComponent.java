package dev.nokee.platform.base;

import org.gradle.api.Action;

/**
 * A component with dependency buckets.
 *
 * @param <T> type of the component dependencies
 * @since 0.4
 */
public interface DependencyAwareComponent<T extends ComponentDependencies> {
	/**
	 * Returns the dependencies of this component.
	 *
	 * @return a {@link ComponentDependencies}, never null.
	 */
	T getDependencies();

	/**
	 * Configure the dependencies of this component.
	 *
	 * @param action configuration action for {@link ComponentDependencies}.
	 */
	void dependencies(Action<? super T> action);
}
